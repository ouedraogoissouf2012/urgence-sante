package com.urgencesante.orientation.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/** Violation d'un invariant du domaine Orientation (donnée invalide). */
public class OrientationValidationException extends ModuleValidationException {

    public OrientationValidationException(String message) {
        super(message);
    }
}
