package com.urgencesante.availability.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/** Violation d'un invariant du domaine Availability (donnée invalide). */
public class AvailabilityValidationException extends ModuleValidationException {

    public AvailabilityValidationException(String message) {
        super(message);
    }
}
