package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Issue #138 : le pool de connexions, l'ouverture de session Hibernate en
 * vue et les threads Tomcat ne doivent pas dépendre des défauts implicites
 * (Hikari maximum-pool-size=10/connection-timeout=30s, open-in-view=true,
 * Tomcat 200 threads) — trop lents (30 s avant rejet) ou trop grands pour ce
 * déploiement (VPS modeste partagé, cf. deploy/docker-compose.yml).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DatabaseAndServerTuningIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.jpa.open-in-view:true}")
    private boolean openInView;

    @Value("${server.tomcat.threads.max:200}")
    private int tomcatThreadsMax;

    @Value("${server.tomcat.threads.min-spare:10}")
    private int tomcatThreadsMinSpare;

    @Test
    void le_pool_de_connexions_est_explicitement_dimensionne() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        final HikariDataSource hikari = (HikariDataSource) dataSource;

        assertThat(hikari.getMaximumPoolSize())
                .as("plafond explicite plutôt que le défaut implicite Hikari")
                .isEqualTo(10);
        assertThat(hikari.getConnectionTimeout())
                .as("échec rapide sous saturation plutôt que 30 s (défaut Hikari)")
                .isEqualTo(2500L);
    }

    @Test
    void open_in_view_est_desactive() {
        assertThat(openInView)
                .as("pas de session Hibernate ouverte jusqu'au rendu de la réponse")
                .isFalse();
    }

    @Test
    void les_threads_tomcat_sont_dimensionnes_explicitement_pour_ce_vps() {
        assertThat(tomcatThreadsMax)
                .as("le défaut Spring Boot (200) est démesuré pour ce VPS partagé")
                .isEqualTo(50);
        assertThat(tomcatThreadsMinSpare).isEqualTo(10);
    }
}
