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

    /**
     * Itinéraires d'une origine vers plusieurs destinations en UN SEUL appel
     * fournisseur (OSRM Table) : la latence ne dépend pas du nombre de
     * destinations. Retourne une liste alignée sur {@code destinations}
     * (élément vide si l'itinéraire est indisponible).
     */
    java.util.List<Optional<Route>> findRoutes(
            Coordinates origin, java.util.List<Coordinates> destinations);
}
