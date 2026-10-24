package com.urgencesante.facility;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * API publique du module Facility. Seul point d'accès autorisé pour les autres
 * modules ; l'implémentation vit dans le package {@code internal}.
 */
public interface FacilityFacade {

    /** Retourne la vue publique d'un établissement, si présent. */
    Optional<FacilityView> findById(UUID id);

    /**
     * Établissements ACCUEILLANT LES URGENCES (2e et 3e niveau de soins — un
     * centre de 1er niveau n'est jamais retourné, ADR-008) et offrant AU MOINS
     * UN des services demandés (sémantique OU), proches d'un point, triés par
     * proximité. Candidats de l'orientation d'urgence : le niveau est filtré
     * AVANT la limite, un centre écarté n'occupe donc aucune place.
     */
    List<FacilityView> findEmergencyCapableNearby(
            Set<String> serviceCodes, double latitude, double longitude, int radiusMeters, int limit);
}
