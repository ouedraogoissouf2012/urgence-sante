package com.urgencesante.routing.internal.adapter.out.osrm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OsrmRouteProviderTest {

    private static final Coordinates ORIGIN = new Coordinates(5.35, -4.00);
    private static final Coordinates DESTINATION = new Coordinates(5.30, -4.05);

    private MockRestServiceServer server;
    private OsrmRouteProvider provider;

    @BeforeEach
    void setUp() {
        final RestClient.Builder builder = RestClient.builder().baseUrl("http://osrm.local");
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new OsrmRouteProvider(builder.build());
    }

    @Test
    void parse_la_distance_et_la_duree() {
        server.expect(requestTo(Matchers.containsString("/route/v1/driving/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"routes\":[{\"distance\":1234.5,\"duration\":678.9}]}",
                        MediaType.APPLICATION_JSON));

        final Optional<Route> route = provider.findRoute(ORIGIN, DESTINATION);

        assertThat(route).isPresent();
        assertThat(route.get().distanceMeters()).isEqualTo(1234.5);
        assertThat(route.get().durationSeconds()).isEqualTo(678.9);
        server.verify();
    }

    @Test
    void retourne_vide_si_aucun_itineraire() {
        server.expect(requestTo(Matchers.containsString("/route/v1/driving/")))
                .andRespond(withSuccess("{\"code\":\"NoRoute\",\"routes\":[]}", MediaType.APPLICATION_JSON));

        assertThat(provider.findRoute(ORIGIN, DESTINATION)).isEmpty();
    }

    @Test
    void retourne_vide_apres_epuisement_des_tentatives_sur_erreur() {
        server.expect(ExpectedCount.times(2), requestTo(Matchers.containsString("/route/v1/driving/")))
                .andRespond(withServerError());

        assertThat(provider.findRoute(ORIGIN, DESTINATION)).isEmpty();
        server.verify();
    }
}
