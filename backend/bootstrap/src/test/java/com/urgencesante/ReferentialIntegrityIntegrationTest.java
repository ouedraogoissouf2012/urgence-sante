package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie sur PostgreSQL réel que les contraintes d'intégrité (migrations V5 et
 * V15) refusent les combinaisons inexistantes, en plus de la validation métier.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReferentialIntegrityIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void un_service_hors_catalogue_est_refuse_par_facility_service() {
        final UUID facilityId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO facility (id, name, location, care_level) "
                        + "VALUES (?, 'Test', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography, "
                        + "'SECONDARY')",
                facilityId);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?, 'inexistant')",
                facilityId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void un_etablissement_sans_niveau_de_soins_est_refuse_par_la_base() {
        // Aucune valeur par défaut : tout chemin d'écriture qui oublierait le
        // niveau échoue bruyamment au lieu de classer le centre en silence.
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO facility (id, name, location) "
                        + "VALUES (?, 'Sans niveau', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography)",
                UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void un_niveau_de_soins_hors_enumeration_est_refuse_par_la_base() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO facility (id, name, location, care_level) "
                        + "VALUES (?, 'Niveau faux', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography, "
                        + "'QUATERNARY')",
                UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void une_disponibilite_sans_offre_est_refusee_par_la_base() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO availability (id, facility_id, service_code, status, updated_at) "
                        + "VALUES (?, ?, 'maternity', 'AVAILABLE', now())",
                UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
