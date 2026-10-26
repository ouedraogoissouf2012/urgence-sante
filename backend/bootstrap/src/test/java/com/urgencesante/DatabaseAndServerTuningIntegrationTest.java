package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
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

    /**
     * Contrat de déploiement : le port d'écoute résolu par la configuration
     * VERSIONNÉE doit être le port conteneur que publient deploy/docker-compose.yml
     * (« 8086:8090 ») et le Dockerfile (EXPOSE), sinon le contrôle de santé du
     * déploiement ne joint jamais l'API. Défaut révélé par la répétition de
     * bascule : server.port n'était déclaré que dans le bloc du profil local —
     * en profil production, Spring Boot écoutait sur 8080 par défaut.
     */
    @Test
    void chaque_profil_deploye_ecoute_sur_le_port_publie_par_le_deploiement() throws IOException {
        final String containerPort = deploymentContainerPort();

        assertThat(serverPortFor("production")).as("profil production").isEqualTo(containerPort);
        assertThat(serverPortFor("local")).as("profil local").isEqualTo(containerPort);
    }

    /** Port résolu pour un profil, à partir des seuls fichiers de configuration versionnés. */
    private static String serverPortFor(String profile) {
        final StandardEnvironment environment = new StandardEnvironment();
        // Ni SERVER_PORT hérité du poste ni propriété système : seul le dépôt compte.
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        ConfigDataEnvironmentPostProcessor.applyTo(
                environment, new DefaultResourceLoader(), new DefaultBootstrapContext(), List.of(profile));
        // 8080 : défaut de Spring Boot quand server.port n'est pas résolu.
        return environment.getProperty("server.port", "8080");
    }

    /** Port conteneur publié par deploy/, vérifié identique dans compose et Dockerfile. */
    private static String deploymentContainerPort() throws IOException {
        final Path deploy = repositoryRoot().resolve("deploy");
        final Matcher published = Pattern.compile("\"\\d+:(\\d+)\"")
                .matcher(Files.readString(deploy.resolve("docker-compose.yml")));
        assertThat(published.find()).as("mapping « hôte:conteneur » dans docker-compose.yml").isTrue();
        final Matcher exposed = Pattern.compile("(?m)^EXPOSE\\s+(\\d+)")
                .matcher(Files.readString(deploy.resolve("Dockerfile")));
        assertThat(exposed.find()).as("EXPOSE dans le Dockerfile").isTrue();
        assertThat(exposed.group(1))
                .as("le Dockerfile et docker-compose.yml publient le même port")
                .isEqualTo(published.group(1));
        return published.group(1);
    }

    private static Path repositoryRoot() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null && !Files.exists(directory.resolve("deploy/docker-compose.yml"))) {
            directory = directory.getParent();
        }
        assertThat(directory).as("racine du dépôt (deploy/docker-compose.yml)").isNotNull();
        return directory;
    }
}
