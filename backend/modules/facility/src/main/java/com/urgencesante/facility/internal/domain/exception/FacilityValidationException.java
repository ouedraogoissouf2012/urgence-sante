package com.urgencesante.facility.internal.domain.exception;

/**
 * Violation d'un invariant du domaine Facility (donnée invalide).
 *
 * <p>Exception non contrôlée, traduite en réponse HTTP à la frontière de
 * l'adaptateur web.
 */
public class FacilityValidationException extends RuntimeException {

    public FacilityValidationException(String message) {
        super(message);
    }
}
