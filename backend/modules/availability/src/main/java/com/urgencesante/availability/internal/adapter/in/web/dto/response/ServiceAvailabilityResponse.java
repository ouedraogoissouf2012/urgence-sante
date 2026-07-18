package com.urgencesante.availability.internal.adapter.in.web.dto.response;

import java.time.Instant;

/** Disponibilité d'un service exposée (conforme au schéma OpenAPI ServiceAvailability). */
public record ServiceAvailabilityResponse(
        String serviceCode,
        String status,
        String freshness,
        Instant updatedAt) {
}
