package com.urgencesante.availability;

import java.time.Instant;

/** Vue publique de la disponibilité d'un service (statut et fraîcheur). */
public record ServiceAvailabilityView(
        String serviceCode,
        String status,
        String freshness,
        Instant updatedAt) {
}
