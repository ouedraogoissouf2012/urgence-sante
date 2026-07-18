package com.urgencesante;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base des tests d'intégration nécessitant une base PostGIS réelle.
 *
 * <p>Deux sources de base, choisies par {@link DatabaseAvailableCondition} :
 * une base EXTERNE fournie par la validation interne ({@code IT_DB_URL},
 * démarrée par {@code scripts/verify-all.sh} via Docker Compose), sinon un
 * conteneur Testcontainers éphémère. Sans aucune des deux, les tests sont
 * ignorés (profil développeur rapide) — jamais silencieusement en validation :
 * la garde {@link DockerAvailabilityGuardTest} et le rapport du script de
 * vérification l'imposent.
 */
@Tag("integration")
@ExtendWith(DatabaseAvailableCondition.class)
abstract class AbstractPostgisIntegrationTest {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres");

    private static PostgreSQLContainer<?> container;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        final String externalUrl = System.getenv("IT_DB_URL");
        if (externalUrl != null && !externalUrl.isBlank()) {
            registry.add("spring.datasource.url", () -> externalUrl);
            registry.add("spring.datasource.username",
                    () -> System.getenv().getOrDefault("IT_DB_USER", "urgence_sante"));
            registry.add("spring.datasource.password",
                    () -> System.getenv().getOrDefault("IT_DB_PASSWORD", ""));
            return;
        }
        // La condition d'exécution garantit ici la disponibilité de Docker.
        if (container == null) {
            container = new PostgreSQLContainer<>(POSTGIS_IMAGE);
            container.start();
        }
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}
