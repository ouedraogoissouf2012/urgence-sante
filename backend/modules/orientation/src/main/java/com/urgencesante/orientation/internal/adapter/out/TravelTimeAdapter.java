package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.orientation.internal.application.port.out.TravelTimePort;
import com.urgencesante.routing.RouteView;
import com.urgencesante.routing.RoutingFacade;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.springframework.stereotype.Component;

/** Obtient les temps de trajet GROUPÉS via l'API publique du module Routing. */
@Component
class TravelTimeAdapter implements TravelTimePort {

    private final RoutingFacade routingFacade;

    TravelTimeAdapter(RoutingFacade routingFacade) {
        this.routingFacade = routingFacade;
    }

    @Override
    public List<OptionalDouble> travelTimesSeconds(
            double fromLat, double fromLon, List<double[]> destinations) {
        return routingFacade.routes(fromLat, fromLon, destinations).stream()
                .map(TravelTimeAdapter::toDuration)
                .toList();
    }

    private static OptionalDouble toDuration(Optional<RouteView> route) {
        return route.map(view -> OptionalDouble.of(view.durationSeconds()))
                .orElseGet(OptionalDouble::empty);
    }
}
