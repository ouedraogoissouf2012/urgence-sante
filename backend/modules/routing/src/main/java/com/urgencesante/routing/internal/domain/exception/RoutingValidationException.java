package com.urgencesante.routing.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/** Violation d'un invariant du domaine Routing (donnée invalide). */
public class RoutingValidationException extends ModuleValidationException {

    public RoutingValidationException(String message) {
        super(message);
    }
}
