package com.urgencesante.identity.internal.application.service;

import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.internal.application.port.in.AuthenticatePortalUseCase;
import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.domain.model.PortalCredential;
import com.urgencesante.identity.internal.domain.model.TokenHasher;
import java.util.Objects;
import java.util.Optional;

/**
 * Authentifie un jeton du portail : calcule l'empreinte du jeton présenté et
 * cherche un identifiant actif correspondant. Aucun jeton en clair n'est
 * conservé ni comparé en mémoire au-delà du hachage.
 */
public class IdentityService implements AuthenticatePortalUseCase {

    private final LoadCredentialPort loadCredentialPort;

    public IdentityService(LoadCredentialPort loadCredentialPort) {
        this.loadCredentialPort = Objects.requireNonNull(loadCredentialPort);
    }

    @Override
    public Optional<PortalPrincipalView> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        final String tokenHash = TokenHasher.sha256Hex(rawToken.trim());
        return loadCredentialPort.findActiveByTokenHash(tokenHash)
                .map(IdentityService::toPrincipal);
    }

    private static PortalPrincipalView toPrincipal(PortalCredential credential) {
        return new PortalPrincipalView(
                credential.id(), credential.label(), credential.role(), credential.facilityId());
    }
}
