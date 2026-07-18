package com.urgencesante.routing;

import java.util.Optional;

/**
 * API publique du module Routing. Permet aux autres modules (ex. orientation)
 * d'obtenir un itinéraire entre deux points, sans connaître le fournisseur.
 */
public interface RoutingFacade {

    Optional<RouteView> route(double fromLat, double fromLon, double toLat, double toLon);
}
