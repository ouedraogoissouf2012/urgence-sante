package com.urgencesante.identity.internal.application.port.out;

import com.urgencesante.identity.internal.domain.model.PortalCredential;
import java.util.Optional;

/** Port sortant : chargement d'un identifiant actif par empreinte de jeton. */
public interface LoadCredentialPort {

    Optional<PortalCredential> findActiveByTokenHash(String tokenHash);
}
