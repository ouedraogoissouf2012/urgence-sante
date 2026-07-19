package com.urgencesante.identity.internal.application.port.in;

import com.urgencesante.identity.PortalPrincipalView;
import java.util.Optional;

/** Cas d'usage entrant : authentifier un jeton du portail. */
public interface AuthenticatePortalUseCase {

    Optional<PortalPrincipalView> authenticate(String rawToken);
}
