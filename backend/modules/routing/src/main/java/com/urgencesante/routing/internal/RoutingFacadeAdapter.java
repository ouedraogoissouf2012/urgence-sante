package com.urgencesante.routing.internal;

import com.urgencesante.routing.RouteView;
import com.urgencesante.routing.RoutingFacade;
import com.urgencesante.routing.internal.application.port.in.GetRouteUseCase;
import com.urgencesante.routing.internal.application.service.RoutingService;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module à partir du cas d'usage. */
@Component
class RoutingFacadeAdapter implements RoutingFacade {

    private final GetRouteUseCase getRoute;
    private final RoutingService routingService;

    RoutingFacadeAdapter(GetRouteUseCase getRoute, RoutingService routingService) {
        this.getRoute = getRoute;
        this.routingService = routingService;
    }

    @Override
    public Optional<RouteView> route(double fromLat, double fromLon, double toLat, double toLon) {
        return getRoute.route(new Coordinates(fromLat, fromLon), new Coordinates(toLat, toLon))
                .map(route -> new RouteView(route.distanceMeters(), route.durationSeconds()));
    }

    @Override
    public List<Optional<RouteView>> routes(
            double fromLat, double fromLon, List<double[]> destinations) {
        final List<Coordinates> coordinates = destinations.stream()
                .map(point -> new Coordinates(point[0], point[1]))
                .toList();
        return routingService.routes(new Coordinates(fromLat, fromLon), coordinates).stream()
                .map(route -> route.map(r -> new RouteView(r.distanceMeters(), r.durationSeconds())))
                .toList();
    }
}
