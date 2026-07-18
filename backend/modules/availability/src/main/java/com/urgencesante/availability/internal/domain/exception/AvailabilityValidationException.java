package com.urgencesante.availability.internal.domain.exception;

/** Violation d'un invariant du domaine Availability (donnée invalide). */
public class AvailabilityValidationException extends RuntimeException {

    public AvailabilityValidationException(String message) {
        super(message);
    }
}
