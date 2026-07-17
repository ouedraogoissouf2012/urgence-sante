package com.urgencesante;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Rend exécutables les frontières du monolithe modulaire.
 *
 * <p>{@link ApplicationModules#verify()} échoue si un module :
 * <ul>
 *   <li>accède à un type non exposé (package {@code internal}) d'un autre module ;</li>
 *   <li>introduit un cycle de dépendances entre modules ;</li>
 *   <li>dépend d'un module non autorisé.</li>
 * </ul>
 *
 * <p>La classe est placée dans le package racine {@code com.urgencesante} afin de
 * ne pas être elle-même détectée comme un module applicatif.
 */
class ModularityTests {

    private static final ApplicationModules MODULES =
            ApplicationModules.of(UrgenceSanteApplication.class);

    @Test
    void les_frontieres_de_modules_sont_respectees() {
        MODULES.verify();
    }
}
