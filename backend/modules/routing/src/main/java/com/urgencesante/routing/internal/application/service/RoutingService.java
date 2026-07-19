package com.urgencesante.routing.internal.application.service;

import com.urgencesante.routing.internal.application.port.in.GetRouteUseCase;
import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import java.util.Objects;
import java.util.Optional;

/**
 * Cas d'usage de calcul d'itinéraire. Java pur : délègue au port fournisseur,
 * injecté par constructeur.
 */
public class RoutingService implements GetRouteUseCase {

    private final RouteProviderPort routeProvider;

    public RoutingService(RouteProviderPort routeProvider) {
        this.routeProvider = Objects.requireNonNull(routeProvider);
    }

    @Override
    public Optional<Route> route(Coordinates origin, Coordinates destination) {
        Objects.requireNonNull(origin, "L'origine est requise");
        Objects.requireNonNull(destination, "La destination est requise");
        return routeProvider.findRoute(origin, destination);
    }

    /** Itinéraires groupés (un appel fournisseur pour toutes les destinations). */
    public java.util.List<Optional<Route>> routes(
            Coordinates origin, java.util.List<Coordinates> destinations) {
        Objects.requireNonNull(origin, "L'origine est requise");
        Objects.requireNonNull(destinations, "Les destinations sont requises");
        return routeProvider.findRoutes(origin, destinations);
    }
}
