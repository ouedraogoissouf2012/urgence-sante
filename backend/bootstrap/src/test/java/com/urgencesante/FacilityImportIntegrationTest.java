package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.facility.internal.application.port.in.ImportFacilitiesUseCase;
import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Import d'annuaire sur PostgreSQL réel : idempotence de l'upsert par clé
 * naturelle, écriture de la provenance et détection des données démo.
 */
@SpringBootTest
@ActiveProfiles("test")
class FacilityImportIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private ImportFacilitiesUseCase importFacilities;

    @Autowired
    private FacilityDirectoryPort directoryPort;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM facility_service WHERE facility_id IN "
                + "(SELECT id FROM facility WHERE source = 'it-src')");
        jdbc.update("DELETE FROM facility WHERE source = 'it-src'");
        // Le catalogue doit connaître le service utilisé.
        jdbc.update("INSERT INTO medical_service (code, label, category) "
                + "VALUES ('emergency', 'Urgences', 'general') ON CONFLICT (code) DO NOTHING");
    }

    private List<FacilityImportRecord> batch() {
        return List.of(new FacilityImportRecord(
                "it-src", "ref-1", "Hôpital Intégration", "+2252722481000",
                5.35, -4.0, Set.of("emergency"), DataStatus.VERIFIED,
                LocalDate.of(2026, 1, 1), "cellule"));
    }

    private Integer countBySource() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM facility WHERE source = 'it-src'", Integer.class);
    }

    @Test
    void upsert_idempotent_et_provenance_persistee() {
        final ImportReport first = importFacilities.importDirectory(batch());
        final ImportReport second = importFacilities.importDirectory(batch());

        assertThat(first.inserted()).isEqualTo(1);
        assertThat(second.updated()).isEqualTo(1);
        assertThat(countBySource()).as("aucun doublon après rejeu").isEqualTo(1);

        final String status = jdbc.queryForObject(
                "SELECT data_status FROM facility WHERE source = 'it-src' AND external_ref = 'ref-1'",
                String.class);
        final java.sql.Date verifiedAt = jdbc.queryForObject(
                "SELECT verified_at FROM facility WHERE source = 'it-src' AND external_ref = 'ref-1'",
                java.sql.Date.class);
        assertThat(status).isEqualTo("VERIFIED");
        assertThat(verifiedAt.toLocalDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void detecte_l_absence_de_donnees_demo() {
        importFacilities.importDirectory(batch());

        assertThat(directoryPort.hasDemoData())
                .as("le lot importé est VERIFIED, pas DEMO")
                .isFalse();
    }

    @Test
    void hasNonDemoData_devient_vrai_des_qu_un_etablissement_reel_existe() {
        importFacilities.importDirectory(batch());

        assertThat(directoryPort.hasNonDemoData())
                .as("un établissement VERIFIED compte comme non-démo")
                .isTrue();
    }

    /**
     * Reproduit le scénario de cutover de l'issue #123 : la démo et le réel
     * coexistent un instant (avant purge), la purge ne doit retirer QUE la démo.
     *
     * <p>ATTENTION : {@code purgeDemoData()} est {@code DELETE FROM facility
     * WHERE data_status = 'DEMO'} SANS filtre de source — global à la base
     * partagée par toute la suite {@code bootstrap} (voir
     * {@code AbstractPostgisIntegrationTest}). C'est le comportement RÉEL et
     * voulu en production (purger toute démo, pas seulement celle d'un import).
     * Aucune autre classe de test ne sème de ligne {@code data_status = DEMO}
     * aujourd'hui (vérifié) ; si une future classe en sème une hors du
     * {@code source = 'it-src'} nettoyé par {@link #clean()}, elle sera
     * emportée par ce test — la coordonner avec cette purge globale plutôt que
     * de la découvrir en échec intermittent.
     */
    @Test
    void purgeDemoData_supprime_la_demo_et_epargne_le_reste() {
        jdbc.update("INSERT INTO facility "
                        + "(id, name, phone, location, source, external_ref, data_status) "
                        + "VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?, ?, ?)",
                UUID.randomUUID(), "Hôpital Démo IT", "+2252722481000",
                -4.0, 5.35, "it-src", "demo-1", DataStatus.DEMO.name());
        importFacilities.importDirectory(batch());

        directoryPort.purgeDemoData();

        assertThat(existsByNaturalKey("it-src", "demo-1")).as("la démo est purgée").isFalse();
        assertThat(existsByNaturalKey("it-src", "ref-1")).as("le réel survit à la purge").isTrue();
    }

    private boolean existsByNaturalKey(String source, String externalRef) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM facility WHERE source = ? AND external_ref = ?)",
                Boolean.class, source, externalRef));
    }
}
