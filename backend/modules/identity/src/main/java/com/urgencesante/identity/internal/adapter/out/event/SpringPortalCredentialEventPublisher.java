package com.urgencesante.identity.internal.adapter.out.event;

import com.urgencesante.identity.PortalCredentialProvisioned;
import com.urgencesante.identity.internal.application.port.out.PortalCredentialEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de publication d'événements, appuyé sur le publisher Spring. Le
 * module audit s'y abonne via un listener {@code @TransactionalEventListener}.
 */
@Component
public class SpringPortalCredentialEventPublisher implements PortalCredentialEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringPortalCredentialEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(PortalCredentialProvisioned event) {
        publisher.publishEvent(event);
    }
}
