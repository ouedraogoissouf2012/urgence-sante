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

    /**
     * Révoque (déconnexion) la session correspondant à ce jeton, avant son
     * expiration naturelle (audit P3 #140 : 90 jours sans révocation
     * possible). IDEMPOTENT : un jeton déjà révoqué, expiré (et déjà purgé)
     * ou inconnu ne produit aucune erreur — l'état final visé (« cette
     * session n'existe plus ») est déjà atteint.
     */
    void revoke(String rawToken);

    /**
     * Supprime les sessions déjà expirées (issue #132 : sans purge, la table
     * grossit sans fin). Renvoie le nombre de sessions supprimées.
     */
    int purgeExpired();
}
