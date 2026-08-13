package com.urgencesante.identity.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/** Violation d'un invariant du domaine Identity (donnée invalide). */
public class IdentityValidationException extends ModuleValidationException {

    public IdentityValidationException(String message) {
        super(message);
    }
}
