package com.urgencesante.routing.internal.adapter.in.web;

import com.urgencesante.routing.internal.adapter.in.web.dto.response.RouteResponse;
import com.urgencesante.routing.internal.adapter.in.web.mapper.RoutingWebMapper;
import com.urgencesante.routing.internal.application.port.in.GetRouteUseCase;
import com.urgencesante.routing.internal.domain.exception.RouteNotFoundException;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST des itinéraires. Conforme au contrat OpenAPI. Aucune
 * logique métier : validation, mapping et appel du port entrant.
 */
@RestController
@RequestMapping("/api/v1/routes")
public class RoutingController {

    private final GetRouteUseCase getRoute;
    private final RoutingWebMapper mapper;

    public RoutingController(GetRouteUseCase getRoute, RoutingWebMapper mapper) {
        this.getRoute = getRoute;
        this.mapper = mapper;
    }

    @GetMapping
    public RouteResponse route(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon) {
        return mapper.toResponse(getRoute
                .route(new Coordinates(fromLat, fromLon), new Coordinates(toLat, toLon))
                .orElseThrow(RouteNotFoundException::new));
    }
}
