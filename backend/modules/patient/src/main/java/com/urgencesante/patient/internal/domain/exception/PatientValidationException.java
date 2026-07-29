package com.urgencesante.patient.internal.domain.exception;

/** Entrée invalide côté domaine patient (téléphone, mot de passe…). */
public class PatientValidationException extends RuntimeException {

    public PatientValidationException(String message) {
        super(message);
    }
}
