package com.urgencesante.audit.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/**
 * Violation d'un invariant du domaine Audit (donnée invalide).
 *
 * <p>Exception non contrôlée. Contrairement aux autres modules, elle n'est
 * traduite à aucune frontière HTTP : Audit n'expose pas d'adaptateur web,
 * seulement un consommateur d'événements. Une occurrence signale un bug dans
 * le mappage événement → commande, pas une entrée utilisateur invalide.
 */
public class AuditValidationException extends ModuleValidationException {

    public AuditValidationException(String message) {
        super(message);
    }
}
