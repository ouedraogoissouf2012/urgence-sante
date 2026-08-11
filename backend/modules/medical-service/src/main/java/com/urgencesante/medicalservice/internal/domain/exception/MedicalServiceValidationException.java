package com.urgencesante.medicalservice.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/** Violation d'un invariant du domaine Medical Service (donnée invalide). */
public class MedicalServiceValidationException extends ModuleValidationException {

    public MedicalServiceValidationException(String message) {
        super(message);
    }
}
