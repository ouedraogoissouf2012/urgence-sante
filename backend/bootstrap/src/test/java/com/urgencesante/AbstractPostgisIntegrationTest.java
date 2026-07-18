package com.urgencesante;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base des tests d'intégration nécessitant une base PostGIS réelle.
 *
 * <p>{@code disabledWithoutDocker = true} constitue le <b>profil développeur
 * rapide</b>, explicite et assumé : sans Docker joignable, ces tests sont
 * ignorés localement. Ce contournement ne peut PAS passer inaperçu en CI :
 * {@link DockerAvailabilityGuardTest} fait échouer le build lorsque
 * {@code REQUIRE_DOCKER_TESTS=true} (positionné par le workflow) et que
 * Docker est indisponible — aucun test critique ne peut être silencieusement
 * ignoré.
 *
 * <p>L'image {@code postgis} est déclarée compatible avec le driver
 * {@code postgres} ; {@link ServiceConnection} câble la source de données Spring
 * vers le conteneur, sans configuration codée en dur.
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractPostgisIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGIS =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("postgis/postgis:16-3.4")
                            .asCompatibleSubstituteFor("postgres"));
}
