package com.urgencesante.availability.internal.application.result;

import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import com.urgencesante.availability.internal.domain.model.Freshness;
import java.time.Instant;

/** Vue applicative de la disponibilité d'un service, avec fraîcheur calculée. */
public record ServiceAvailabilitySnapshot(
        String serviceCode,
        AvailabilityStatus status,
        Freshness freshness,
        Instant updatedAt) {
}
