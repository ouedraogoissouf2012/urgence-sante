package com.urgencesante.identity.internal.domain.model;

import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.domain.exception.IdentityValidationException;
import java.util.UUID;

/**
 * Identifiant du portail : empreinte de jeton, rôle et portée.
 *
 * @param facilityId établissement pour un opérateur ; {@code null} pour un admin
 */
public record PortalCredential(
        UUID id, String label, String tokenHash, PortalRole role, UUID facilityId, boolean active) {

    /**
     * Crée un nouveau credential actif. Valide le couple rôle/établissement
     * selon la même règle que la contrainte {@code chk_operator_scope} en base
     * (V7__create_portal_credential.sql) : un FACILITY_OPERATOR est rattaché à
     * un établissement, un ADMIN ne l'est à aucun — pour rejeter l'incohérence
     * ici avec un message clair plutôt que de laisser remonter une
     * DataIntegrityViolationException brute depuis la contrainte SQL.
     */
    public static PortalCredential provision(UUID id, String label, String tokenHash, PortalRole role, UUID facilityId) {
        if (id == null) {
            throw new IdentityValidationException("L'identifiant du credential est requis.");
        }
        if (label == null || label.isBlank()) {
            throw new IdentityValidationException("Le libellé du credential ne peut pas être vide.");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IdentityValidationException("Le hachage du jeton est requis.");
        }
        if (role == null) {
            throw new IdentityValidationException("Le rôle du credential est requis.");
        }
        if (role == PortalRole.FACILITY_OPERATOR && facilityId == null) {
            throw new IdentityValidationException(
                    "Un credential FACILITY_OPERATOR doit être rattaché à un établissement (facilityId requis).");
        }
        if (role == PortalRole.ADMIN && facilityId != null) {
            throw new IdentityValidationException(
                    "Un credential ADMIN ne doit pas être rattaché à un établissement (facilityId doit être null).");
        }
        return new PortalCredential(id, label.trim(), tokenHash, role, facilityId, true);
    }
}
