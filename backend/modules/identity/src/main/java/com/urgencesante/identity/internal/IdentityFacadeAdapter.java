package com.urgencesante.identity.internal;

import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.application.command.ProvisionCredentialCommand;
import com.urgencesante.identity.internal.application.port.in.AuthenticatePortalUseCase;
import com.urgencesante.identity.internal.application.port.in.ProvisionPortalCredentialUseCase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module à partir des cas d'usage. */
@Component
class IdentityFacadeAdapter implements IdentityFacade {

    private final AuthenticatePortalUseCase authenticatePortal;
    private final ProvisionPortalCredentialUseCase provisionPortalCredential;

    IdentityFacadeAdapter(
            AuthenticatePortalUseCase authenticatePortal,
            ProvisionPortalCredentialUseCase provisionPortalCredential) {
        this.authenticatePortal = authenticatePortal;
        this.provisionPortalCredential = provisionPortalCredential;
    }

    @Override
    public Optional<PortalPrincipalView> authenticate(String rawToken) {
        return authenticatePortal.authenticate(rawToken);
    }

    @Override
    public NewPortalCredential provision(String label, PortalRole role, UUID facilityId) {
        return provisionPortalCredential.provision(new ProvisionCredentialCommand(label, role, facilityId));
    }
}
