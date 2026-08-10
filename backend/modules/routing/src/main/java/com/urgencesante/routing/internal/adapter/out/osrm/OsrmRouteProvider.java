package com.urgencesante.routing.internal.adapter.out.osrm;

import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import com.urgencesante.routing.internal.domain.resilience.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
 *
 * <p>Un appel réussi mais LENT (au-delà de {@code slow-call-threshold-ms}) est
 * compté comme un échec du circuit : une dégradation soutenue du fournisseur
 * ouvre le circuit (repli « temps estimé » immédiat) au lieu de payer la latence
 * à chaque orientation et de retenir les threads servlet (issue #125).
 */
@Component
public class OsrmRouteProvider implements RouteProviderPort {

    private static final Logger LOG = LoggerFactory.getLogger(OsrmRouteProvider.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Counter errorCounter;
    private final long slowCallThresholdMs;
    private final LongSupplier nanoTime;

    @Autowired
    public OsrmRouteProvider(
            @Qualifier("osrmRestClient") RestClient restClient,
            CircuitBreaker circuitBreaker,
            MeterRegistry meterRegistry,
            @Value("${routing.osrm.slow-call-threshold-ms:2500}") long slowCallThresholdMs) {
        this(restClient, circuitBreaker, meterRegistry, slowCallThresholdMs, System::nanoTime);
    }

    /** Constructeur testable : source de temps injectable (mesure de latence). */
    OsrmRouteProvider(
            RestClient restClient,
            CircuitBreaker circuitBreaker,
            MeterRegistry meterRegistry,
            long slowCallThresholdMs,
            LongSupplier nanoTime) {
        this.restClient = restClient;
        this.circuitBreaker = circuitBreaker;
        this.errorCounter = meterRegistry.counter("osrm.errors");
        this.slowCallThresholdMs = slowCallThresholdMs;
        this.nanoTime = nanoTime;
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

    /**
     * Exécute l'appel sous disjoncteur ; vide si circuit ouvert ou échec.
     *
     * <p>TOUTE exception est traitée comme un échec du circuit, pas seulement
     * {@link RestClientException}. C'est essentiel : {@code allowRequest()} pose
     * un essai « en vol » en état semi-ouvert, que seul {@code recordSuccess} ou
     * {@code recordFailure} relâche. Si une exception inattendue (réponse OSRM
     * malformée → {@code IndexOutOfBoundsException}, désérialisation, etc.)
     * échappait sans appeler {@code recordFailure}, l'essai resterait bloqué et
     * le circuit refuserait tout appel futur À VIE. On garantit donc la
     * libération de l'essai quelle que soit la trajectoire d'erreur.
     *
     * <p>Un succès trop LENT (au-delà du seuil) est également compté comme un
     * échec — le résultat obtenu reste utilisé (il est valide), mais la lenteur
     * pénalise le circuit pour basculer en mode dégradé si elle persiste (#125).
     */
    private <T> Optional<T> guarded(Supplier<T> call) {
        if (!circuitBreaker.allowRequest()) {
            return Optional.empty();
        }
        final long startNanos = nanoTime.getAsLong();
        try {
            final T result = call.get();
            final long elapsedMs = (nanoTime.getAsLong() - startNanos) / 1_000_000L;
            if (elapsedMs > slowCallThresholdMs) {
                circuitBreaker.recordFailure();
                errorCounter.increment();
                LOG.warn("OSRM lent ({} ms > {} ms) : compté comme échec (circuit {})",
                        elapsedMs, slowCallThresholdMs, circuitBreaker.state());
            } else {
                circuitBreaker.recordSuccess();
            }
            return Optional.of(result);
        } catch (RuntimeException exception) {
            circuitBreaker.recordFailure();
            errorCounter.increment();
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
