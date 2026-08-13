package com.urgencesante.identity.internal.application.port.out;

import com.urgencesante.identity.internal.domain.model.PortalCredential;

/** Persistance des identifiants du portail (port sortant). */
public interface SaveCredentialPort {

    void save(PortalCredential credential);
}
