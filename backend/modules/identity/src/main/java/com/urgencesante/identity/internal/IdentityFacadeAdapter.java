package com.urgencesante.identity.internal;

import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.internal.application.port.in.AuthenticatePortalUseCase;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module à partir du cas d'usage. */
@Component
class IdentityFacadeAdapter implements IdentityFacade {

    private final AuthenticatePortalUseCase authenticatePortal;

    IdentityFacadeAdapter(AuthenticatePortalUseCase authenticatePortal) {
        this.authenticatePortal = authenticatePortal;
    }

    @Override
    public Optional<PortalPrincipalView> authenticate(String rawToken) {
        return authenticatePortal.authenticate(rawToken);
    }
}
