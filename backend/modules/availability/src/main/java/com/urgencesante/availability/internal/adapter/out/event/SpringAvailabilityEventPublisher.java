package com.urgencesante.availability.internal.adapter.out.event;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.port.out.AvailabilityEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de publication d'événements, appuyé sur le publisher Spring. Les
 * modules réactifs (audit, notification) s'y abonnent via des listeners.
 */
@Component
public class SpringAvailabilityEventPublisher implements AvailabilityEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringAvailabilityEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(AvailabilityUpdated event) {
        publisher.publishEvent(event);
    }
}
