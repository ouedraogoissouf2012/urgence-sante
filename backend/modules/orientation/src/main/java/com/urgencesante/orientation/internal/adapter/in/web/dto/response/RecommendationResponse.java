package com.urgencesante.orientation.internal.adapter.in.web.dto.response;

/**
 * Recommandation exposée par l'API (conforme au schéma OpenAPI Recommendation).
 *
 * @param travelTimeSeconds absent si le temps de trajet est indisponible
 */
public record RecommendationResponse(
        String facilityId,
        String name,
        double distanceMeters,
        Double travelTimeSeconds,
        String status,
        String explanation) {
}
