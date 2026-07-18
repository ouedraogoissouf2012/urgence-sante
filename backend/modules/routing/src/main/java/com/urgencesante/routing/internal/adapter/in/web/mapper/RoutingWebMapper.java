package com.urgencesante.routing.internal.adapter.in.web.mapper;

import com.urgencesante.routing.internal.adapter.in.web.dto.response.RouteResponse;
import com.urgencesante.routing.internal.domain.model.Route;
import org.springframework.stereotype.Component;

/** Traduit le domaine en DTO de réponse HTTP. */
@Component
public class RoutingWebMapper {

    public RouteResponse toResponse(Route route) {
        return new RouteResponse(route.distanceMeters(), route.durationSeconds());
    }
}
