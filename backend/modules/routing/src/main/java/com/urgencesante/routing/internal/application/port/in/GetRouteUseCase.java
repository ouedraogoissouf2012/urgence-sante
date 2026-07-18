package com.urgencesante.routing.internal.application.port.in;

import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import java.util.Optional;

/** Port entrant : calculer un itinéraire entre deux points. */
public interface GetRouteUseCase {

    Optional<Route> route(Coordinates origin, Coordinates destination);
}
