package com.urgencesante.buildingblocks.exception;

/**
 * Base des exceptions de validation de domaine (donnée invalide) levées par
 * les modules métier. Chaque module dérive son propre type — pour la
 * typesafety et la lisibilité des traces — sans dupliquer la logique de
 * transport du message ; tout comportement commun futur (ex. code d'erreur,
 * horodatage) s'ajoute ici, en un seul endroit.
 */
public class ModuleValidationException extends RuntimeException {

    public ModuleValidationException(String message) {
        super(message);
    }
}
