package com.urgencesante.patient.internal.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Sessions patient (port sortant).
 *
 * <p>Émet un jeton porteur opaque pour un compte authentifié, le persiste sous
 * forme d'empreinte (jamais en clair) et renvoie le jeton en clair UNE fois, à
 * remettre au client. Résout aussi un jeton présenté en identifiant de patient
 * si la session est valide et non expirée. L'algorithme (aléatoire, empreinte,
 * expiration) est du ressort de l'implémentation.
 */
public interface PatientSessionPort {

    /** Ouvre une session pour le compte et renvoie le jeton porteur en clair. */
    String issueToken(UUID patientId);

    /** Identifiant du patient si le jeton correspond à une session non expirée. */
    Optional<UUID> resolvePatient(String rawToken);
}
