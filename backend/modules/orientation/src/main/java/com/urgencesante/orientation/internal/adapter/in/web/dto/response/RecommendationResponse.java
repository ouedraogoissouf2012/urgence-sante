package com.urgencesante.orientation.internal.adapter.in.web.dto.response;

/**
 * Recommandation exposée par l'API (conforme au schéma OpenAPI Recommendation).
 *
 * @param travelTimeSeconds absent si le temps de trajet est indisponible
 * @param phone absent si le téléphone du centre est inconnu
 */
public record RecommendationResponse(
        String facilityId,
        String name,
        GeoPointResponse location,
        String phone,
        double distanceMeters,
        Double travelTimeSeconds,
        String travelTimeQuality,
        String status,
        String explanation) {

    /** Point géographique (conforme au schéma OpenAPI GeoPoint). */
    public record GeoPointResponse(double latitude, double longitude) {
    }
}
