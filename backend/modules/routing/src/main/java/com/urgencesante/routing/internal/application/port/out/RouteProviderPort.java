package com.urgencesante.routing.internal.application.port.out;

import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import java.util.Optional;

/**
 * Port sortant : fournisseur d'itinéraires, indépendant de toute technologie
 * (OSRM, GraphHopper, faux fournisseur de test…).
 *
 * <p>Retourne un résultat vide lorsqu'aucun itinéraire n'est disponible ou en
 * cas d'échec contrôlé du fournisseur (jamais d'exception d'infrastructure).
 */
public interface RouteProviderPort {

    Optional<Route> findRoute(Coordinates origin, Coordinates destination);
}
