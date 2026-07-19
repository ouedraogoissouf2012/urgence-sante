package com.urgencesante.routing.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RoutingServiceTest {

    private static final Coordinates ORIGIN = new Coordinates(5.35, -4.00);
    private static final Coordinates DESTINATION = new Coordinates(5.30, -4.05);

    /** Faux fournisseur substituable au vrai adaptateur OSRM. */
    private static RouteProviderPort provider(Route single) {
        return new RouteProviderPort() {
            @Override
            public Optional<Route> findRoute(Coordinates origin, Coordinates destination) {
                return Optional.ofNullable(single);
            }

            @Override
            public java.util.List<Optional<Route>> findRoutes(
                    Coordinates origin, java.util.List<Coordinates> destinations) {
                return destinations.stream().map(d -> Optional.ofNullable(single)).toList();
            }
        };
    }

    @Test
    void delegue_au_fournisseur_et_retourne_l_itineraire() {
        final RoutingService service = new RoutingService(provider(new Route(1200.0, 180.0)));

        final Optional<Route> route = service.route(ORIGIN, DESTINATION);

        assertThat(route).contains(new Route(1200.0, 180.0));
    }

    @Test
    void propage_l_absence_d_itineraire() {
        final RoutingService service = new RoutingService(provider(null));

        assertThat(service.route(ORIGIN, DESTINATION)).isEmpty();
    }

    @Test
    void itineraires_groupes_alignes_sur_les_destinations() {
        final RoutingService service = new RoutingService(provider(new Route(500.0, 60.0)));

        final var routes = service.routes(ORIGIN, java.util.List.of(DESTINATION, ORIGIN));

        assertThat(routes).hasSize(2);
        assertThat(routes.get(0)).contains(new Route(500.0, 60.0));
    }
}
