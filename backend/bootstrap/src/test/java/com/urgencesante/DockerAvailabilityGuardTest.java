package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

/**
 * Garde anti-« skip silencieux » des tests d'intégration PostGIS.
 *
 * <p>Quand l'environnement exige les tests d'intégration
 * ({@code REQUIRE_DOCKER_TESTS=true}, positionné par la CI), l'indisponibilité
 * de Docker fait ÉCHOUER le build au lieu de laisser les tests s'ignorer.
 * Sans cette variable (poste développeur), la garde est neutre : le profil
 * rapide local reste possible mais reste visible dans les rapports (skipped).
 */
class DockerAvailabilityGuardTest {

    @Test
    void docker_doit_etre_disponible_quand_les_tests_d_integration_sont_exiges() {
        final boolean required = Boolean.parseBoolean(
                System.getenv().getOrDefault("REQUIRE_DOCKER_TESTS", "false"));
        Assumptions.assumeTrue(required,
                "Garde inactive : REQUIRE_DOCKER_TESTS absent (profil développeur rapide)");

        assertThat(DockerClientFactory.instance().isDockerAvailable())
                .as("REQUIRE_DOCKER_TESTS=true mais Docker est injoignable : les tests "
                        + "PostGIS seraient silencieusement ignorés — échec volontaire")
                .isTrue();
    }
}
