package com.urgencesante.identity.internal.application.port.in;

import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.internal.application.command.ProvisionCredentialCommand;

/**
 * Cas d'usage entrant : provisionner un nouveau credential portail (admin ou
 * opérateur). Renvoie directement le type public {@link NewPortalCredential}
 * — même choix que {@link AuthenticatePortalUseCase}, qui renvoie déjà
 * {@code PortalPrincipalView} (public) sans type interne intermédiaire.
 */
public interface ProvisionPortalCredentialUseCase {

    NewPortalCredential provision(ProvisionCredentialCommand command);
}
