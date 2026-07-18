package com.urgencesante.orientation.internal.domain.model;

import java.util.UUID;

/**
 * Recommandation d'un établissement : score de classement, statut interprété et
 * explication lisible.
 *
 * @param travelTimeSeconds temps de trajet, ou {@code null} si indisponible
 */
public record Recommendation(
        UUID facilityId,
        String name,
        double distanceMeters,
        Double travelTimeSeconds,
        String status,
        double score,
        String explanation) {
}
