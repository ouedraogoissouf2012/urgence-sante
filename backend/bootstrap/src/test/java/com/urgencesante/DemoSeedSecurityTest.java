package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * Garde de sécurité (issue #124) : le jeu de démonstration livré ne doit JAMAIS
 * contenir de credential opérateur. Un jeton (même haché) versionné dans le seed
 * est embarqué dans l'artefact (JAR) et devient actif partout où ce jeu est
 * chargé — profil local, y compris la démo déployée — autorisant l'écriture de
 * disponibilité sur TOUT établissement. Le jeton de démo est désormais
 * provisionné HORS du dépôt par les scripts locaux (scripts/local-up.sh &
 * e2e-smoke.sh, variable PORTAL_TOKEN).
 *
 * <p>Test pur (aucune base de données) : il lit le seed livré tel qu'il sera
 * embarqué et empaqueté.
 */
class DemoSeedSecurityTest {

    private static String seed() throws IOException {
        return StreamUtils.copyToString(
                        new ClassPathResource("db/local-seed/R__seed_demo.sql").getInputStream(),
                        StandardCharsets.UTF_8)
                .toLowerCase();
    }

    @Test
    void le_seed_livre_ne_contient_aucun_credential_operateur() throws IOException {
        assertThat(seed())
                .as("le seed de démo ne doit contenir aucun INSERT dans portal_credential "
                        + "(un secret versionné devient actif en production — issue #124)")
                .doesNotContain("insert into portal_credential");
    }

    @Test
    void le_seed_purge_l_ancien_jeton_de_demonstration_au_rechargement() throws IOException {
        assertThat(seed())
                .as("le seed doit purger tout jeton opérateur de démo résiduel (VPS) au "
                        + "rechargement de la migration répétable")
                .contains("delete from portal_credential");
    }
}
