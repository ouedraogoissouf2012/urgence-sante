package com.urgencesante;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie que l'application démarre avec le profil de test : le contexte Spring
 * se charge et l'assemblage des modules est cohérent. Ce test échoue si un
 * module casse le démarrage (bean manquant, configuration invalide).
 */
@SpringBootTest
@ActiveProfiles("test")
class UrgenceSanteApplicationTests {

    @Test
    void le_contexte_demarre_avec_le_profil_test() {
        // Le simple chargement du contexte suffit : une erreur ferait échouer le test.
    }
}
