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

    @Test
    void delegue_au_fournisseur_et_retourne_l_itineraire() {
        // Faux fournisseur substituable au vrai adaptateur OSRM.
        final RouteProviderPort fakeProvider = (origin, destination) -> Optional.of(new Route(1200.0, 180.0));
        final RoutingService service = new RoutingService(fakeProvider);

        final Optional<Route> route = service.route(ORIGIN, DESTINATION);

        assertThat(route).contains(new Route(1200.0, 180.0));
    }

    @Test
    void propage_l_absence_d_itineraire() {
        final RouteProviderPort emptyProvider = (origin, destination) -> Optional.empty();
        final RoutingService service = new RoutingService(emptyProvider);

        assertThat(service.route(ORIGIN, DESTINATION)).isEmpty();
    }
}
