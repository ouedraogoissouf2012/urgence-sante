package com.urgencesante.routing.internal.domain.exception;

/** Violation d'un invariant du domaine Routing (donnée invalide). */
public class RoutingValidationException extends RuntimeException {

    public RoutingValidationException(String message) {
        super(message);
    }
}
