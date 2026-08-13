package com.urgencesante.patient.internal.application.port.in;

/** Révocation (déconnexion) d'une session patient (port entrant). */
public interface RevokePatientSessionUseCase {

    /**
     * Invalide le jeton présenté avant son expiration naturelle. Idempotent :
     * sans effet si le jeton ne correspond à aucune session active.
     */
    void revoke(String rawToken);
}
