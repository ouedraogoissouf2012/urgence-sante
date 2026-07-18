package com.urgencesante.orientation.internal.domain.model;

import java.util.UUID;

/**
 * Données d'un candidat à évaluer par les stratégies : proximité, statut de
 * disponibilité (déjà interprété : un statut périmé est ramené à « UNKNOWN »)
 * et temps de trajet éventuel.
 *
 * @param travelTimeSeconds temps de trajet, ou {@code null} si indisponible
 */
public record CandidateEvaluation(
        UUID facilityId,
        String name,
        double latitude,
        double longitude,
        double distanceMeters,
        String status,
        String freshness,
        Double travelTimeSeconds) {

    public boolean hasTravelTime() {
        return travelTimeSeconds != null;
    }
}
