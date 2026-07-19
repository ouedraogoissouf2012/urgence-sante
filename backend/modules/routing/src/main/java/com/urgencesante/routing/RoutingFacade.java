package com.urgencesante.routing;

import java.util.Optional;

/**
 * API publique du module Routing. Permet aux autres modules (ex. orientation)
 * d'obtenir un itinéraire entre deux points, sans connaître le fournisseur.
 */
public interface RoutingFacade {

    Optional<RouteView> route(double fromLat, double fromLon, double toLat, double toLon);

    /**
     * Itinéraires d'une origine vers plusieurs destinations en UN appel
     * fournisseur. Liste alignée sur {@code destinations} (vide si
     * indisponible).
     *
     * @param destinations couples {latitude, longitude}
     */
    java.util.List<Optional<RouteView>> routes(
            double fromLat, double fromLon, java.util.List<double[]> destinations);
}
