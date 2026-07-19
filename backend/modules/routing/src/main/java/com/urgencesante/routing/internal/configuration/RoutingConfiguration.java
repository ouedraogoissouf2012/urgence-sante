package com.urgencesante.routing.internal.configuration;

import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.application.service.RoutingService;
import com.urgencesante.routing.internal.domain.resilience.CircuitBreaker;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Assemblage Spring du module Routing : client OSRM (délais bornés
 * configurables), disjoncteur partagé et cas d'usage.
 */
@Configuration
public class RoutingConfiguration {

    /** Client HTTP vers OSRM — le budget de latence est borné par ces délais. */
    @Bean
    RestClient osrmRestClient(
            RestClient.Builder builder,
            @Value("${routing.osrm.base-url:https://router.project-osrm.org}") String baseUrl,
            @Value("${routing.osrm.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${routing.osrm.read-timeout-ms:5000}") int readTimeoutMs) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    /**
     * Disjoncteur OSRM partagé : après {@code failure-threshold} échecs
     * consécutifs, les appels sont refusés {@code open-duration-ms} sans
     * latence réseau, puis un essai de reprise est autorisé.
     */
    @Bean
    CircuitBreaker osrmCircuitBreaker(
            Clock clock,
            @Value("${routing.osrm.circuit.failure-threshold:3}") int failureThreshold,
            @Value("${routing.osrm.circuit.open-duration-ms:30000}") long openDurationMs) {
        return new CircuitBreaker(failureThreshold, Duration.ofMillis(openDurationMs), clock);
    }

    @Bean
    RoutingService routingService(RouteProviderPort routeProvider) {
        return new RoutingService(routeProvider);
    }
}
