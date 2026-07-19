package com.urgencesante.routing.internal.adapter.out.osrm;

import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import com.urgencesante.routing.internal.domain.resilience.CircuitBreaker;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptateur OSRM du port fournisseur d'itinéraires.
 *
 * <p>Résilience : timeouts bornés par le client HTTP, DISJONCTEUR partagé
 * (circuit ouvert → réponse vide immédiate, aucune latence réseau), et pour le
 * calcul groupé UN SEUL appel /table quel que soit le nombre de destinations —
 * une panne ne multiplie jamais la latence par le nombre de centres.
 */
@Component
public class OsrmRouteProvider implements RouteProviderPort {

    private static final Logger LOG = LoggerFactory.getLogger(OsrmRouteProvider.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public OsrmRouteProvider(
            @Qualifier("osrmRestClient") RestClient restClient, CircuitBreaker circuitBreaker) {
        this.restClient = restClient;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Optional<Route> findRoute(Coordinates origin, Coordinates destination) {
        final URI uri = URI.create("/route/v1/driving/"
                + coords(List.of(origin, destination)) + "?overview=false");
        return guarded(() -> toRoute(
                restClient.get().uri(uri).retrieve().body(OsrmResponse.class)))
                .orElse(Optional.empty());
    }

    @Override
    public List<Optional<Route>> findRoutes(Coordinates origin, List<Coordinates> destinations) {
        if (destinations.isEmpty()) {
            return List.of();
        }
        final List<Coordinates> all = new ArrayList<>();
        all.add(origin);
        all.addAll(destinations);
        final String dests = IntStream.rangeClosed(1, destinations.size())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(";"));
        final URI uri = URI.create("/table/v1/driving/" + coords(all)
                + "?sources=0&destinations=" + dests + "&annotations=duration,distance");

        return guarded(() -> toRoutes(
                restClient.get().uri(uri).retrieve().body(OsrmTableResponse.class),
                destinations.size()))
                .orElseGet(() -> Collections.nCopies(destinations.size(), Optional.empty()));
    }

    /** Exécute l'appel sous disjoncteur ; vide si circuit ouvert ou échec. */
    private <T> Optional<T> guarded(Supplier<T> call) {
        if (!circuitBreaker.allowRequest()) {
            return Optional.empty();
        }
        try {
            final T result = call.get();
            circuitBreaker.recordSuccess();
            return Optional.of(result);
        } catch (RestClientException exception) {
            circuitBreaker.recordFailure();
            LOG.warn("OSRM indisponible (circuit {}) : {}",
                    circuitBreaker.state(), exception.getMessage());
            return Optional.empty();
        }
    }

    private static String coords(List<Coordinates> points) {
        return points.stream()
                .map(p -> String.format(Locale.ROOT, "%.6f,%.6f", p.longitude(), p.latitude()))
                .collect(Collectors.joining(";"));
    }

    private static Optional<Route> toRoute(OsrmResponse response) {
        if (response == null
                || !"Ok".equalsIgnoreCase(response.code())
                || response.routes() == null
                || response.routes().isEmpty()) {
            return Optional.empty();
        }
        final OsrmResponse.OsrmRoute first = response.routes().get(0);
        return Optional.of(new Route(first.distance(), first.duration()));
    }

    private static List<Optional<Route>> toRoutes(OsrmTableResponse response, int expected) {
        if (response == null
                || !"Ok".equalsIgnoreCase(response.code())
                || response.durations() == null
                || response.durations().isEmpty()) {
            return Collections.nCopies(expected, Optional.empty());
        }
        final List<Double> durations = response.durations().get(0);
        final List<Double> distances = response.distances() == null || response.distances().isEmpty()
                ? Collections.nCopies(expected, (Double) null)
                : response.distances().get(0);

        final List<Optional<Route>> routes = new ArrayList<>(expected);
        for (int i = 0; i < expected; i++) {
            final Double duration = i < durations.size() ? durations.get(i) : null;
            final Double distance = i < distances.size() ? distances.get(i) : null;
            routes.add(duration == null
                    ? Optional.empty()
                    : Optional.of(new Route(distance == null ? 0.0 : distance, duration)));
        }
        return routes;
    }
}
