package com.urgencesante.patient.internal.domain.exception;

/** Un compte existe déjà pour ce numéro de téléphone (→ HTTP 409). */
public class PhoneAlreadyRegisteredException extends RuntimeException {

    public PhoneAlreadyRegisteredException() {
        super("Un compte existe déjà pour ce numéro de téléphone");
    }
}
