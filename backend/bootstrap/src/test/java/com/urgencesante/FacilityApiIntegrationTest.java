package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie de bout en bout, sur PostGIS réel, l'endpoint de recherche des
 * établissements : filtrage par service et tri par proximité.
 *
 * <p>Ignoré si Docker n'est pas joignable (voir {@link AbstractPostgisIntegrationTest}).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FacilityApiIntegrationTest extends AbstractPostgisIntegrationTest {

    // Abidjan (Cocody) et San-Pédro (~300 km) pour un contraste de proximité net.
    private static final double COCODY_LAT = 5.35;
    private static final double COCODY_LON = -4.00;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM facility_service");
        jdbc.update("DELETE FROM facility");
        insertFacility(UUID.randomUUID(), "CHU de Cocody", COCODY_LAT, COCODY_LON, "maternity");
        insertFacility(UUID.randomUUID(), "Hôpital de San-Pédro", 4.75, -6.63, "surgery");
    }

    private void insertFacility(UUID id, String name, double lat, double lon, String service) {
        jdbc.update(
                "INSERT INTO facility (id, name, phone, location) "
                        + "VALUES (?, ?, NULL, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography)",
                id, name, lon, lat);
        jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?, ?)", id, service);
    }

    @Test
    void liste_tous_les_etablissements_sans_filtre() {
        final var response = rest.getForEntity("/api/v1/facilities", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("CHU de Cocody").contains("Hôpital de San-Pédro");
    }

    @Test
    void filtre_par_service_medical() {
        final var response = rest.getForEntity("/api/v1/facilities?service=surgery", String.class);

        assertThat(response.getBody())
                .contains("Hôpital de San-Pédro")
                .doesNotContain("CHU de Cocody");
    }

    @Test
    void trie_et_filtre_par_proximite() {
        final var response = rest.getForEntity(
                "/api/v1/facilities?lat=" + COCODY_LAT + "&lon=" + COCODY_LON + "&radiusMeters=10000",
                String.class);

        // Seul l'établissement dans le rayon est retourné, avec sa distance.
        assertThat(response.getBody())
                .contains("CHU de Cocody")
                .doesNotContain("San-Pédro")
                .contains("distanceMeters");
    }
}
