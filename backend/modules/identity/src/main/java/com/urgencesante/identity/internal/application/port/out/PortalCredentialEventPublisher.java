package com.urgencesante.identity.internal.application.port.out;

import com.urgencesante.identity.PortalCredentialProvisioned;

/** Port sortant : publie l'événement public de provisioning d'un credential. */
public interface PortalCredentialEventPublisher {

    void publish(PortalCredentialProvisioned event);
}
