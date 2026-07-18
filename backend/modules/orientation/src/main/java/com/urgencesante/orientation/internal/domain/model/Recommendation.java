package com.urgencesante.orientation.internal.domain.model;

import java.util.UUID;

/**
 * Recommandation d'un établissement : score de classement, statut interprété et
 * explication lisible, avec les données de fiche (position, téléphone).
 *
 * @param travelTimeSeconds temps de trajet, ou {@code null} si indisponible
 * @param phone téléphone du centre, ou {@code null} si inconnu
 */
public record Recommendation(
        UUID facilityId,
        String name,
        double latitude,
        double longitude,
        String phone,
        double distanceMeters,
        Double travelTimeSeconds,
        String status,
        double score,
        String explanation) {
}
