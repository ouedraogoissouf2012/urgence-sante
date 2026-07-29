package com.urgencesante.patient.internal.domain.exception;

/**
 * Identifiants de connexion invalides (→ HTTP 401). Message volontairement
 * générique : ne révèle PAS si c'est le téléphone ou le mot de passe qui est
 * en cause (anti-énumération de comptes).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Numéro de téléphone ou mot de passe incorrect");
    }
}
