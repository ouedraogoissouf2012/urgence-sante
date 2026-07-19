package com.urgencesante.identity;

import java.util.Optional;
import java.util.UUID;

/**
 * Identité authentifiée d'un porteur de jeton du portail (vue publique).
 *
 * @param facilityId établissement de rattachement pour un opérateur ; vide
 *     pour un administrateur (portée globale)
 */
public record PortalPrincipalView(String label, PortalRole role, UUID facilityId) {

    /** Vrai si ce porteur est autorisé à agir sur {@code targetFacilityId}. */
    public boolean canActOn(UUID targetFacilityId) {
        return role == PortalRole.ADMIN
                || (facilityId != null && facilityId.equals(targetFacilityId));
    }

    public Optional<UUID> facility() {
        return Optional.ofNullable(facilityId);
    }
}
