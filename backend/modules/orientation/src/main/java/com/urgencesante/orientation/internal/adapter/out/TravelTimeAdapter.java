package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.orientation.internal.application.port.out.TravelTimePort;
import com.urgencesante.routing.RoutingFacade;
import java.util.OptionalDouble;
import org.springframework.stereotype.Component;

/** Obtient le temps de trajet via l'API publique du module Routing. */
@Component
class TravelTimeAdapter implements TravelTimePort {

    private final RoutingFacade routingFacade;

    TravelTimeAdapter(RoutingFacade routingFacade) {
        this.routingFacade = routingFacade;
    }

    @Override
    public OptionalDouble travelTimeSeconds(double fromLat, double fromLon, double toLat, double toLon) {
        return routingFacade.route(fromLat, fromLon, toLat, toLon)
                .map(route -> OptionalDouble.of(route.durationSeconds()))
                .orElseGet(OptionalDouble::empty);
    }
}
