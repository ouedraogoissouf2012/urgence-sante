package com.urgencesante;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base des tests d'intégration nécessitant une base PostGIS réelle.
 *
 * <p>{@code disabledWithoutDocker = true} : si aucun environnement Docker n'est
 * joignable, les tests de cette hiérarchie sont <em>ignorés</em> plutôt qu'en
 * échec. Ils s'exécutent réellement en CI (runner Linux) et sur toute machine
 * où Docker est disponible pour Testcontainers.
 *
 * <p>L'image {@code postgis} est déclarée compatible avec le driver
 * {@code postgres} ; {@link ServiceConnection} câble la source de données Spring
 * vers le conteneur, sans configuration codée en dur.
 */
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractPostgisIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGIS =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("postgis/postgis:16-3.4")
                            .asCompatibleSubstituteFor("postgres"));
}
