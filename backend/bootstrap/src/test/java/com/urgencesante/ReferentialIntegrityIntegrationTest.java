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
 * Vérifie sur PostgreSQL réel que les contraintes d'intégrité (migration V5)
 * refusent les combinaisons inexistantes, en plus de la validation métier.
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
                "INSERT INTO facility (id, name, location) "
                        + "VALUES (?, 'Test', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography)",
                facilityId);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?, 'inexistant')",
                facilityId))
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
