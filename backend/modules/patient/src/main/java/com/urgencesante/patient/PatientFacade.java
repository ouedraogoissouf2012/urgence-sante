package com.urgencesante.patient;

import java.util.Optional;
import java.util.UUID;

/**
 * API publique du module Patient. Seul point d'entrée pour les autres modules
 * et l'assemblage (bootstrap) — l'implémentation reste dans {@code internal}.
 *
 * <p>Aujourd'hui, expose la résolution d'une session en identifiant de patient
 * (pour un futur intercepteur d'authentification patient). L'inscription et la
 * connexion passent par le contrôleur REST du module.
 */
public interface PatientFacade {

    /** Identifiant du patient si le jeton porteur correspond à une session valide. */
    Optional<UUID> authenticate(String rawToken);
}
