package com.urgencesante.orientation.internal;

import com.urgencesante.orientation.OrientationFacade;
import com.urgencesante.orientation.RecommendationView;
import com.urgencesante.orientation.internal.application.port.in.RecommendFacilitiesUseCase;
import com.urgencesante.orientation.internal.application.query.OrientationQuery;
import java.util.List;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module à partir du cas d'usage. */
@Component
class OrientationFacadeAdapter implements OrientationFacade {

    private final RecommendFacilitiesUseCase recommendFacilities;

    OrientationFacadeAdapter(RecommendFacilitiesUseCase recommendFacilities) {
        this.recommendFacilities = recommendFacilities;
    }

    @Override
    public List<RecommendationView> recommend(
            double latitude, double longitude, String serviceCode, int radiusMeters, int limit) {
        return recommendFacilities
                .recommend(new OrientationQuery(latitude, longitude, serviceCode, radiusMeters, limit))
                .stream()
                .map(recommendation -> new RecommendationView(
                        recommendation.facilityId(),
                        recommendation.name(),
                        recommendation.distanceMeters(),
                        recommendation.travelTimeSeconds(),
                        recommendation.status(),
                        recommendation.explanation()))
                .toList();
    }
}
