package com.urgencesante.patient.internal.domain.exception;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;

/** Entrée invalide côté domaine patient (téléphone, mot de passe…). */
public class PatientValidationException extends ModuleValidationException {

    public PatientValidationException(String message) {
        super(message);
    }
}
