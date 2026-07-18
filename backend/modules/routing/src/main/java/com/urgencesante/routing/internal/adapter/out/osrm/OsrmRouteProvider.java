package com.urgencesante.routing.internal.adapter.out.osrm;

import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.domain.model.Coordinates;
import com.urgencesante.routing.internal.domain.model.Route;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptateur OSRM du port fournisseur d'itinéraires.
 *
 * <p>Erreurs contrôlées : timeout et retry portés par le client HTTP et une
 * boucle de tentatives ; toute erreur d'infrastructure est traduite en résultat
 * vide (aucun itinéraire), jamais propagée. Aucun accès base de données.
 */
@Component
public class OsrmRouteProvider implements RouteProviderPort {

    private static final Logger LOG = LoggerFactory.getLogger(OsrmRouteProvider.class);
    private static final int MAX_ATTEMPTS = 2;

    private final RestClient restClient;

    public OsrmRouteProvider(@Qualifier("osrmRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<Route> findRoute(Coordinates origin, Coordinates destination) {
        // OSRM attend l'ordre lon,lat ; format neutre pour éviter les virgules locales.
        final String coordinates = String.format(
                Locale.ROOT, "%.6f,%.6f;%.6f,%.6f",
                origin.longitude(), origin.latitude(),
                destination.longitude(), destination.latitude());
        final URI uri = URI.create("/route/v1/driving/" + coordinates + "?overview=false");

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return toRoute(restClient.get().uri(uri).retrieve().body(OsrmResponse.class));
            } catch (RestClientException exception) {
                LOG.warn("OSRM indisponible (tentative {}/{}) : {}", attempt, MAX_ATTEMPTS, exception.getMessage());
            }
        }
        return Optional.empty();
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
}
