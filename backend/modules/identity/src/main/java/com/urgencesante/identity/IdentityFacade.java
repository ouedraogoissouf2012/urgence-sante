package com.urgencesante.identity;

import java.util.Optional;
import java.util.UUID;

/**
 * API publique du module Identity : authentifie un jeton du portail et
 * provisionne de nouveaux identifiants.
 *
 * <p>Ne révèle aucun type interne. L'appelant (couche d'assemblage) décide de
 * la réponse HTTP (401 si vide, 403 selon la portée).
 */
public interface IdentityFacade {

    /**
     * Authentifie un jeton présenté en clair.
     *
     * @return l'identité si le jeton correspond à un identifiant actif ; vide
     *     sinon (jeton absent, inconnu ou désactivé)
     */
    Optional<PortalPrincipalView> authenticate(String rawToken);

    /**
     * Provisionne un nouveau credential portail (issue #164). {@code facilityId}
     * est requis pour {@link PortalRole#FACILITY_OPERATOR}, refusé pour
     * {@link PortalRole#ADMIN}.
     *
     * @throws com.urgencesante.buildingblocks.exception.ModuleValidationException
     *     si le libellé est vide ou si le couple rôle/établissement est invalide
     */
    NewPortalCredential provision(String label, PortalRole role, UUID facilityId);
}
