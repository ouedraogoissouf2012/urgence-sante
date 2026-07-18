package com.urgencesante.orientation;

import java.util.UUID;

/**
 * Vue publique d'une recommandation d'orientation.
 *
 * @param travelTimeSeconds absent ({@code null}) si le temps de trajet est indisponible
 */
public record RecommendationView(
        UUID facilityId,
        String name,
        double distanceMeters,
        Double travelTimeSeconds,
        String status,
        String explanation) {
}
