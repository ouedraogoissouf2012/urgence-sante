package com.urgencesante.routing.internal;

import com.urgencesante.routing.RouteView;
import com.urgencesante.routing.RoutingFacade;
import com.urgencesante.routing.internal.application.port.in.GetRouteUseCase;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module à partir du cas d'usage. */
@Component
class RoutingFacadeAdapter implements RoutingFacade {

    private final GetRouteUseCase getRoute;

    RoutingFacadeAdapter(GetRouteUseCase getRoute) {
        this.getRoute = getRoute;
    }

    @Override
    public Optional<RouteView> route(double fromLat, double fromLon, double toLat, double toLon) {
        return getRoute.route(new Coordinates(fromLat, fromLon), new Coordinates(toLat, toLon))
                .map(route -> new RouteView(route.distanceMeters(), route.durationSeconds()));
    }
}
