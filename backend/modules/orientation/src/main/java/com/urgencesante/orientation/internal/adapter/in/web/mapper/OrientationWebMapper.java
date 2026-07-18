package com.urgencesante.orientation.internal.adapter.in.web.mapper;

import com.urgencesante.orientation.internal.adapter.in.web.dto.response.RecommendationResponse;
import com.urgencesante.orientation.internal.domain.model.Recommendation;
import org.springframework.stereotype.Component;

/** Traduit le domaine en DTO de réponse HTTP, sans logique métier. */
@Component
public class OrientationWebMapper {

    public RecommendationResponse toResponse(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.facilityId().toString(),
                recommendation.name(),
                Math.round(recommendation.distanceMeters() * 10.0) / 10.0,
                recommendation.travelTimeSeconds(),
                recommendation.status(),
                recommendation.explanation());
    }
}
