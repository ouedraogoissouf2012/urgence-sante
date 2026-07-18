package com.urgencesante;

import com.tngtech.archunit.core.domain.JavaClass;
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

    // building-blocks est un socle partagé (kernel), pas un module métier : il
    // est exclu de la détection de modules pour rester librement réutilisable.
    private static final ApplicationModules MODULES = ApplicationModules.of(
            UrgenceSanteApplication.class,
            JavaClass.Predicates.resideInAnyPackage("com.urgencesante.buildingblocks.."));

    @Test
    void les_frontieres_de_modules_sont_respectees() {
        MODULES.verify();
    }
}
