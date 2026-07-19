package com.urgencesante.facility.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.application.port.out.KnownServicePort;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FacilityImportServiceTest {

    /** Faux annuaire en mémoire, avec upsert idempotent par clé naturelle. */
    private static final class FakeDirectory implements FacilityDirectoryPort {
        final Set<String> keys = new HashSet<>();

        @Override
        public boolean existsByNaturalKey(String source, String externalRef) {
            return keys.contains(source + '/' + externalRef);
        }

        @Override
        public void upsert(FacilityImportRecord record) {
            keys.add(record.source() + '/' + record.externalRef());
        }

        @Override
        public boolean hasDemoData() {
            return false;
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
}
