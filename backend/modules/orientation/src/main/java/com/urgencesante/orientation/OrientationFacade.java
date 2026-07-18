package com.urgencesante.orientation;

import java.util.List;

/**
 * API publique du module Orientation : recommander des établissements pour un
 * besoin médical à partir d'une position.
 */
public interface OrientationFacade {

    List<RecommendationView> recommend(
            double latitude, double longitude, String serviceCode, int radiusMeters, int limit);
}
