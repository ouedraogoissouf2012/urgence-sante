package com.urgencesante.identity.internal.domain.model;

import com.urgencesante.identity.PortalRole;
import java.util.UUID;

/**
 * Identifiant du portail : empreinte de jeton, rôle et portée.
 *
 * @param facilityId établissement pour un opérateur ; {@code null} pour un admin
 */
public record PortalCredential(
        UUID id, String label, String tokenHash, PortalRole role, UUID facilityId, boolean active) {
}
