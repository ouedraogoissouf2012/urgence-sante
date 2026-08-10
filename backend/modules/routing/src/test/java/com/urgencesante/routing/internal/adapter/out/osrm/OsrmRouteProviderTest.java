package com.urgencesante.routing.internal.adapter.out.osrm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import com.urgencesante.routing.internal.domain.resilience.CircuitBreaker;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OsrmRouteProviderTest {

    private static final Coordinates ORIGIN = new Coordinates(5.35, -4.00);
    private static final Coordinates DEST_A = new Coordinates(5.30, -4.05);
    private static final Coordinates DEST_B = new Coordinates(5.40, -3.95);

    private MockRestServiceServer server;
    private OsrmRouteProvider provider;
    private RestClient.Builder builder;
    private Clock clock;
    private Instant now;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder().baseUrl("http://osrm.local");
        server = MockRestServiceServer.bindTo(builder).build();
        now = Instant.parse("2026-01-01T12:00:00Z");
        clock = new Clock() {
            @Override public Instant instant() { return now; }
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        };
        // Source de temps constante (0) : les appels du serveur mock sont
        // instantanés, jamais « lents » → les tests existants gardent leur
        // comportement (le seuil de lenteur n'entre en jeu que dans son test dédié).
        provider = new OsrmRouteProvider(
                builder.build(), new CircuitBreaker(3, Duration.ofSeconds(30), clock),
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry(), 2500L, () -> 0L);
    }

    @Test
    void itineraire_simple_parse_distance_et_duree() {
        server.expect(requestTo(Matchers.containsString("/route/v1/driving/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"routes\":[{\"distance\":1234.5,\"duration\":678.9}]}",
                        MediaType.APPLICATION_JSON));

        final Optional<Route> route = provider.findRoute(ORIGIN, DEST_A);

        assertThat(route).contains(new Route(1234.5, 678.9));
        server.verify();
    }

    @Test
    void appel_groupe_un_seul_appel_table_pour_plusieurs_destinations() {
        server.expect(ExpectedCount.once(), requestTo(Matchers.containsString("/table/v1/driving/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"durations\":[[300.0,null]],"
                                + "\"distances\":[[2500.0,null]]}",
                        MediaType.APPLICATION_JSON));

        final List<Optional<Route>> routes = provider.findRoutes(ORIGIN, List.of(DEST_A, DEST_B));

        assertThat(routes).hasSize(2);
        assertThat(routes.get(0)).contains(new Route(2500.0, 300.0));
        assertThat(routes.get(1)).isEmpty();
        server.verify();
    }

    @Test
    void circuit_ouvert_apres_trois_echecs_puis_reponses_vides_sans_reseau() {
        // 3 échecs → circuit OUVERT. Aucune requête supplémentaire attendue :
        // le 4e appel (20 destinations) répond vide IMMÉDIATEMENT.
        server.expect(ExpectedCount.times(3), requestTo(Matchers.containsString("/table/")))
                .andRespond(withServerError());

        for (int i = 0; i < 3; i++) {
            assertThat(provider.findRoutes(ORIGIN, List.of(DEST_A))).allMatch(Optional::isEmpty);
        }
        final List<Optional<Route>> afterOpen =
                provider.findRoutes(ORIGIN, java.util.Collections.nCopies(20, DEST_A));

        assertThat(afterOpen).hasSize(20).allMatch(Optional::isEmpty);
        server.verify(); // aucune requête au-delà des 3 échecs
    }

    @Test
    void une_erreur_non_reseau_ne_bloque_pas_le_circuit_a_vie() {
        // Régression C1 : en semi-ouvert, allowRequest() pose un essai « en vol »
        // que seul record{Success,Failure} relâche. Si l'essai lève une exception
        // NON-réseau (réponse OSRM syntaxiquement Ok mais au JSON incompatible →
        // erreur de désérialisation, pas une RestClientException), l'ancien code
        // ne la rattrapait pas : l'essai restait bloqué et le circuit refusait
        // tout appel À VIE. On prouve ici que le circuit reste opérationnel.

        // 3 échecs réseau → circuit OUVERT.
        server.expect(ExpectedCount.times(3), requestTo(Matchers.containsString("/table/")))
                .andRespond(withServerError());
        // Après le délai, l'essai semi-ouvert reçoit un corps au type incompatible
        // ("durations" doit être un tableau de tableaux de nombres) → exception de
        // conversion, NON-RestClientException.
        server.expect(ExpectedCount.once(), requestTo(Matchers.containsString("/table/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"durations\":\"pas-un-tableau\"}",
                        MediaType.APPLICATION_JSON));
        // Le circuit doit accepter un NOUVEL essai plus tard (preuve : non bloqué).
        server.expect(ExpectedCount.once(), requestTo(Matchers.containsString("/table/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"durations\":[[150.0]],\"distances\":[[800.0]]}",
                        MediaType.APPLICATION_JSON));

        for (int i = 0; i < 3; i++) {
            provider.findRoutes(ORIGIN, List.of(DEST_A));
        }
        now = now.plus(Duration.ofSeconds(31)); // délai écoulé → 1er essai (malformé)
        assertThat(provider.findRoutes(ORIGIN, List.of(DEST_A))).allMatch(Optional::isEmpty);

        // Sans le correctif, l'essai resterait « en vol » et cet appel serait
        // refusé sans requête. Avec le correctif, un nouvel essai est autorisé.
        now = now.plus(Duration.ofSeconds(31));
        final List<Optional<Route>> recovered = provider.findRoutes(ORIGIN, List.of(DEST_A));

        assertThat(recovered.get(0)).contains(new Route(800.0, 150.0));
        server.verify();
    }

    @Test
    void reprise_apres_le_delai_d_ouverture() {
        server.expect(ExpectedCount.times(3), requestTo(Matchers.containsString("/table/")))
                .andRespond(withServerError());
        server.expect(ExpectedCount.once(), requestTo(Matchers.containsString("/table/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"durations\":[[120.0]],\"distances\":[[900.0]]}",
                        MediaType.APPLICATION_JSON));

        for (int i = 0; i < 3; i++) {
            provider.findRoutes(ORIGIN, List.of(DEST_A));
        }
        now = now.plus(Duration.ofSeconds(31)); // délai écoulé → essai autorisé

        final List<Optional<Route>> recovered = provider.findRoutes(ORIGIN, List.of(DEST_A));

        assertThat(recovered.get(0)).contains(new Route(900.0, 120.0));
        server.verify();
    }

    @Test
    void un_appel_lent_est_compte_comme_echec_et_ouvre_le_circuit() {
        // 3 appels RÉUSSIS mais LENTS (au-delà du seuil) → 3 échecs → circuit
        // OUVERT ; le 4e est court-circuité (réponse vide, aucune requête). La
        // lenteur soutenue bascule bien en mode dégradé au lieu de payer la
        // latence — et de retenir des threads — à chaque orientation (#125).
        server.expect(ExpectedCount.times(3), requestTo(Matchers.containsString("/table/")))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"durations\":[[150.0]],\"distances\":[[800.0]]}",
                        MediaType.APPLICATION_JSON));

        final long thresholdMs = 1000;
        // nanoTime est lu 2× par appel (début, fin) : on renvoie une latence de
        // 2 s (> seuil) à chaque appel, sans dormir (test déterministe).
        final AtomicInteger reads = new AtomicInteger();
        final LongSupplier slowNanos =
                () -> (reads.getAndIncrement() % 2 == 0) ? 0L : 2_000_000_000L;

        final OsrmRouteProvider slow = new OsrmRouteProvider(
                builder.build(), new CircuitBreaker(3, Duration.ofSeconds(30), clock),
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry(),
                thresholdMs, slowNanos);

        for (int i = 0; i < 3; i++) {
            // L'appel réussit (route valide) MAIS est compté comme un échec (lent).
            assertThat(slow.findRoutes(ORIGIN, List.of(DEST_A)).get(0))
                    .contains(new Route(800.0, 150.0));
        }
        // Circuit OUVERT : 4e appel court-circuité, aucune requête réseau.
        assertThat(slow.findRoutes(ORIGIN, List.of(DEST_A))).allMatch(Optional::isEmpty);

        server.verify(); // exactement 3 requêtes
    }
}
