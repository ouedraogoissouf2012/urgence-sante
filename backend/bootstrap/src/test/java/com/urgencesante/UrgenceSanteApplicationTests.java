package com.urgencesante;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie que l'application démarre avec le profil de test, base PostGIS réelle
 * comprise : le contexte Spring se charge, la source de données se connecte et
 * les migrations Flyway s'appliquent. Ce test échoue si un module casse le
 * démarrage ou si une migration est invalide.
 *
 * <p>Ignoré automatiquement si Docker n'est pas joignable (voir
 * {@link AbstractPostgisIntegrationTest}).
 */
@SpringBootTest
@ActiveProfiles("test")
class UrgenceSanteApplicationTests extends AbstractPostgisIntegrationTest {

    @Test
    void le_contexte_demarre_avec_le_profil_test() {
        // Le chargement du contexte (avec DB + Flyway) suffit : une erreur
        // ferait échouer le test.
    }
}
