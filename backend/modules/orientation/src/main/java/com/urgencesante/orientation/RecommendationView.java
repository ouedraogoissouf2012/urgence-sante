package com.urgencesante.orientation;

import java.util.UUID;

/**
 * Vue publique d'une recommandation d'orientation.
 *
 * @param travelTimeSeconds absent ({@code null}) si le temps de trajet est indisponible
 * @param phone absent ({@code null}) si le téléphone du centre est inconnu
 */
public record RecommendationView(
        UUID facilityId,
        String name,
        double latitude,
        double longitude,
        String phone,
        double distanceMeters,
        Double travelTimeSeconds,
        String status,
        String explanation) {
}
