package com.urgencesante.availability.internal.application.port.out;

import com.urgencesante.availability.AvailabilityUpdated;

/** Port sortant : publie l'événement public de mise à jour de disponibilité. */
public interface AvailabilityEventPublisher {

    void publish(AvailabilityUpdated event);
}
