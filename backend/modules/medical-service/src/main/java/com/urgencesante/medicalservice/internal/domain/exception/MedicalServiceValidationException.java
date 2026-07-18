package com.urgencesante.medicalservice.internal.domain.exception;

/** Violation d'un invariant du domaine Medical Service (donnée invalide). */
public class MedicalServiceValidationException extends RuntimeException {

    public MedicalServiceValidationException(String message) {
        super(message);
    }
}
