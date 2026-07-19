package com.urgencesante.identity;

import java.util.Optional;

/**
 * API publique du module Identity : authentifie un jeton du portail.
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
}
