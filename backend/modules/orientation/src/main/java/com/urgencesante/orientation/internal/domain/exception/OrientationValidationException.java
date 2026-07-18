package com.urgencesante.orientation.internal.domain.exception;

/** Violation d'un invariant du domaine Orientation (donnée invalide). */
public class OrientationValidationException extends RuntimeException {

    public OrientationValidationException(String message) {
        super(message);
    }
}
