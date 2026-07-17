package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie, sur une base PostGIS réelle, que Flyway exécute une migration
 * complète et que l'extension géospatiale est disponible.
 *
 * <p>Ignoré automatiquement si Docker n'est pas joignable (voir
 * {@link AbstractPostgisIntegrationTest}).
 */
@SpringBootTest
@ActiveProfiles("test")
class PostgisMigrationIntegrationTests extends AbstractPostgisIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flyway_a_applique_au_moins_une_migration() {
        Integer applied = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true",
                Integer.class);

        assertThat(applied).isNotNull().isGreaterThanOrEqualTo(1);
    }

    @Test
    void l_extension_postgis_est_active() {
        String version = jdbc.queryForObject(
                "SELECT extversion FROM pg_extension WHERE extname = 'postgis'",
                String.class);

        assertThat(version).isNotBlank();
    }
}
