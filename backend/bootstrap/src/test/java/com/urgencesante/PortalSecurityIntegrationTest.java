package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.identity.internal.domain.model.TokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Sécurité de la mise à jour de disponibilité sur base réelle : 401 sans
 * jeton, 403 hors périmètre, 200 avec un jeton opérateur du bon établissement.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PortalSecurityIntegrationTest extends AbstractPostgisIntegrationTest {

    private static final String FACILITY = "aaaaaaaa-0000-0000-0000-000000000001";
    private static final String OTHER = "aaaaaaaa-0000-0000-0000-000000000002";
    private static final String TOKEN = "operator-facility-1-token";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM portal_credential WHERE label = 'IT'");
        jdbc.update("DELETE FROM facility_service WHERE facility_id = ?::uuid", FACILITY);
        jdbc.update("DELETE FROM facility WHERE id = ?::uuid", FACILITY);
        jdbc.update(
                "INSERT INTO facility (id, name, location) VALUES "
                        + "(?::uuid, 'IT Facility', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography)",
                FACILITY);
        jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?::uuid, 'maternity')",
                FACILITY);
        jdbc.update(
                "INSERT INTO portal_credential (id, label, token_hash, role, facility_id, active) "
                        + "VALUES (gen_random_uuid(), 'IT', ?, 'FACILITY_OPERATOR', ?::uuid, TRUE)",
                TokenHasher.sha256Hex(TOKEN), FACILITY);
    }

    private ResponseEntity<String> put(String facility, String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange(
                "/api/v1/facilities/" + facility + "/availability/maternity",
                HttpMethod.PUT,
                new HttpEntity<>("{\"status\":\"AVAILABLE\"}", headers),
                String.class);
    }

    @Test
    void sans_jeton_401() {
        assertThat(put(FACILITY, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void hors_perimetre_403() {
        assertThat(put(OTHER, TOKEN).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void operateur_du_bon_etablissement_200() {
        assertThat(put(FACILITY, TOKEN).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
