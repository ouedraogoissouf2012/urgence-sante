package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie les états de santé sur base réelle : démarré (liveness), prêt
 * (readiness incluant la base — une base indisponible ferait basculer ce
 * groupe), et signalement OSRM distinct de la readiness.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void liveness_et_readiness_sont_up_et_la_readiness_inclut_la_base() {
        final ResponseEntity<String> liveness =
                rest.getForEntity("/actuator/health/liveness", String.class);
        final ResponseEntity<String> readiness =
                rest.getForEntity("/actuator/health/readiness", String.class);

        assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(liveness.getBody()).contains("UP");
        assertThat(readiness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readiness.getBody()).contains("UP")
                .as("la base fait partie du groupe readiness")
                .contains("db");
    }

    @Test
    void le_composant_osrm_est_signale_hors_readiness() {
        final ResponseEntity<String> health =
                rest.getForEntity("/actuator/health", String.class);
        final ResponseEntity<String> readiness =
                rest.getForEntity("/actuator/health/readiness", String.class);

        assertThat(health.getBody()).contains("osrm");
        assertThat(readiness.getBody())
                .as("OSRM dégradé ne doit jamais faire tomber la readiness")
                .doesNotContain("osrm");
    }
}
