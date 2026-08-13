package com.urgencesante.identity;

import java.util.UUID;

/**
 * Credential portail nouvellement provisionné, tel qu'exposé HORS du module :
 * porte le jeton en clair — {@code rawToken} n'est disponible qu'ICI, une
 * seule fois, jamais re-consultable ensuite (seule son empreinte est
 * persistée).
 *
 * <p>{@code toString()} est délibérément redéfini pour NE JAMAIS inclure
 * {@code rawToken} : le {@code toString()} généré par défaut pour un record
 * inclurait tous les composants, ce qui exposerait le jeton en clair au
 * premier {@code log.debug(credential)} ou échec d'assertion de test
 * imprimant cet objet — exactement ce que cette classe existe pour éviter.
 */
public record NewPortalCredential(UUID id, String label, PortalRole role, UUID facilityId, String rawToken) {

    @Override
    public String toString() {
        return "NewPortalCredential[id=%s, label=%s, role=%s, facilityId=%s, rawToken=***REDACTED***]"
                .formatted(id, label, role, facilityId);
    }
}
