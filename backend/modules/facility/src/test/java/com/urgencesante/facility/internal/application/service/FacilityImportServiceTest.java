package com.urgencesante.facility.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.application.port.out.KnownServicePort;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FacilityImportServiceTest {

    /**
     * Faux annuaire en mémoire, avec upsert idempotent par clé naturelle.
     * Trace le {@link DataStatus} de chaque établissement (pas seulement sa
     * présence) : {@link #hasDemoData()}/{@link #hasNonDemoData()} doivent
     * refléter le statut réel pour que {@code purgeDemoDataIfReplaced()} soit
     * exercé fidèlement par ce double de test.
     */
    private static final class FakeDirectory implements FacilityDirectoryPort {
        final Map<String, DataStatus> byKey = new LinkedHashMap<>();

        @Override
        public boolean existsByNaturalKey(String source, String externalRef) {
            return byKey.containsKey(source + '/' + externalRef);
        }

        @Override
        public void upsert(FacilityImportRecord record) {
            byKey.put(record.source() + '/' + record.externalRef(), record.dataStatus());
        }

        @Override
        public boolean hasDemoData() {
            return byKey.containsValue(DataStatus.DEMO);
        }

        @Override
        public boolean hasNonDemoData() {
            return byKey.values().stream().anyMatch(status -> status != DataStatus.DEMO);
        }

        @Override
        public void purgeDemoData() {
            byKey.values().removeIf(status -> status == DataStatus.DEMO);
        }
    }

    private final KnownServicePort knownServices =
            code -> Set.of("emergency", "maternity").contains(code);

    private FacilityImportRecord valid(String ref, String name, double lat, double lon,
            DataStatus status) {
        return new FacilityImportRecord("src", ref, name, "+2252722481000", lat, lon,
                Set.of("emergency"), status, LocalDate.of(2026, 1, 1), "resp");
    }

    @Test
    void insere_puis_rejouer_le_meme_lot_met_a_jour_sans_doublon() {
        final FakeDirectory directory = new FakeDirectory();
        final FacilityImportService service =
                new FacilityImportService(directory, knownServices, false);
        final List<FacilityImportRecord> batch = List.of(
                valid("a", "Hôpital A", 5.35, -4.0, DataStatus.VERIFIED),
                valid("b", "Hôpital B", 5.30, -3.95, DataStatus.VERIFIED));

        final ImportReport first = service.importDirectory(batch);
        final ImportReport second = service.importDirectory(batch);

        assertThat(first.inserted()).isEqualTo(2);
        assertThat(first.updated()).isZero();
        // Idempotence : le rejeu met à jour, n'insère rien de nouveau.
        assertThat(second.inserted()).isZero();
        assertThat(second.updated()).isEqualTo(2);
    }

    @Test
    void rejette_les_doublons_de_cle_dans_le_lot() {
        final FacilityImportService service =
                new FacilityImportService(new FakeDirectory(), knownServices, false);

        final ImportReport report = service.importDirectory(List.of(
                valid("a", "Hôpital A", 5.35, -4.0, DataStatus.VERIFIED),
                valid("a", "Hôpital A bis", 5.36, -4.01, DataStatus.VERIFIED)));

        assertThat(report.inserted()).isEqualTo(1);
        assertThat(report.rejectedCount()).isEqualTo(1);
        assertThat(report.rejected().get(0).reasons()).anyMatch(r -> r.contains("doublon"));
    }

    @Test
    void rejette_un_quasi_doublon_meme_nom_et_position_proche() {
        final FacilityImportService service =
                new FacilityImportService(new FakeDirectory(), knownServices, false);

        final ImportReport report = service.importDirectory(List.of(
                valid("a", "Hôpital A", 5.3500, -4.0000, DataStatus.VERIFIED),
                valid("b", "Hôpital A", 5.35005, -4.00005, DataStatus.VERIFIED)));

        assertThat(report.inserted()).isEqualTo(1);
        assertThat(report.rejected().get(0).reasons()).anyMatch(r -> r.contains("quasi-doublon"));
    }

    @Test
    void refuse_les_donnees_demo_en_production() {
        final FacilityImportService production =
                new FacilityImportService(new FakeDirectory(), knownServices, true);

        final ImportReport report = production.importDirectory(List.of(
                valid("a", "Hôpital DEMO", 5.35, -4.0, DataStatus.DEMO)));

        assertThat(report.inserted()).isZero();
        assertThat(report.rejected().get(0).reasons())
                .anyMatch(r -> r.contains("démonstration interdites en production"));
    }

    @Test
    void accepte_les_donnees_demo_hors_production() {
        final FacilityImportService dev =
                new FacilityImportService(new FakeDirectory(), knownServices, false);

        final ImportReport report = dev.importDirectory(List.of(
                valid("a", "Hôpital DEMO", 5.35, -4.0, DataStatus.DEMO)));

        assertThat(report.inserted()).isEqualTo(1);
    }

    /**
     * Coeur de la garde de sécurité issue #123 : un import réussi (au moins un
     * établissement non-démo accepté) purge la démo résiduelle.
     */
    @Test
    void purgeDemoDataIfReplaced_purge_quand_l_import_a_reussi() {
        final FakeDirectory directory = new FakeDirectory();
        // Démo pré-existante (simule un ancien déploiement local mal configuré).
        directory.upsert(new FacilityImportRecord(
                "demo-src", "demo-1", "Hôpital DEMO", "+2252722481000",
                5.35, -4.0, Set.of("emergency"), DataStatus.DEMO, null, "resp"));
        final FacilityImportService service =
                new FacilityImportService(directory, knownServices, true);

        service.importDirectory(List.of(
                valid("a", "Hôpital A", 5.35, -4.0, DataStatus.VERIFIED)));
        final boolean purged = service.purgeDemoDataIfReplaced();

        assertThat(purged).as("import réussi -> purge autorisée").isTrue();
        assertThat(directory.hasDemoData()).as("la démo a été retirée").isFalse();
    }

    /**
     * Garde de sécurité issue #123 : SANS établissement non-démo en annuaire
     * (import totalement raté, ou jamais lancé), la purge doit être refusée —
     * ne jamais vider l'annuaire.
     */
    @Test
    void purgeDemoDataIfReplaced_refuse_si_aucune_donnee_non_demo() {
        final FakeDirectory directory = new FakeDirectory();
        directory.upsert(new FacilityImportRecord(
                "demo-src", "demo-1", "Hôpital DEMO", "+2252722481000",
                5.35, -4.0, Set.of("emergency"), DataStatus.DEMO, null, "resp"));
        final FacilityImportService service =
                new FacilityImportService(directory, knownServices, true);

        // Lot entièrement rejeté (hors zone d'Abidjan) : rien d'accepté.
        final ImportReport report = service.importDirectory(List.of(
                valid("a", "Hôpital hors zone", 0.0, 0.0, DataStatus.VERIFIED)));
        final boolean purged = service.purgeDemoDataIfReplaced();

        assertThat(report.inserted()).isZero();
        assertThat(purged).as("import raté -> purge refusée").isFalse();
        assertThat(directory.hasDemoData()).as("la démo survit à un import raté").isTrue();
    }
}
