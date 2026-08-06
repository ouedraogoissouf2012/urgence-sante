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
     * Établissements offrant un service donné, proches d'un point, triés par
     * proximité. Cas mono-service, équivalent à {@link #findNearbyOfferingAny}
     * avec un ensemble singleton.
     */
    List<FacilityView> findNearbyOffering(
            String serviceCode, double latitude, double longitude, int radiusMeters, int limit);

    /**
     * Établissements offrant AU MOINS UN des services demandés (sémantique OU),
     * proches d'un point, triés par proximité. Utilisé par l'orientation quand un
     * symptôme couvre plusieurs spécialités.
     */
    List<FacilityView> findNearbyOfferingAny(
            Set<String> serviceCodes, double latitude, double longitude, int radiusMeters, int limit);
}
