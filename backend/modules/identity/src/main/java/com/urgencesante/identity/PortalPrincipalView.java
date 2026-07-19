package com.urgencesante.identity;

import java.util.Optional;
import java.util.UUID;

/**
 * Identité authentifiée d'un porteur de jeton du portail (vue publique).
 *
 * @param id identifiant immuable et unique de l'identifiant (clé de débit fiable)
 * @param facilityId établissement de rattachement pour un opérateur ; vide
 *     pour un administrateur (portée globale)
 */
public record PortalPrincipalView(UUID id, String label, PortalRole role, UUID facilityId) {

    /** Vrai si ce porteur est autorisé à agir sur {@code targetFacilityId}. */
    public boolean canActOn(UUID targetFacilityId) {
        return role == PortalRole.ADMIN
                || (facilityId != null && facilityId.equals(targetFacilityId));
    }

    public Optional<UUID> facility() {
        return Optional.ofNullable(facilityId);
    }
}
