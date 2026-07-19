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
}
