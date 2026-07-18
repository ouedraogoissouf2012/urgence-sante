package com.urgencesante.routing.internal.configuration;

import com.urgencesante.routing.internal.application.port.out.RouteProviderPort;
import com.urgencesante.routing.internal.application.service.RoutingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Assemblage Spring du module Routing (client OSRM et cas d'usage). */
@Configuration
public class RoutingConfiguration {

    private static final int CONNECT_TIMEOUT_MS = 2_000;
    private static final int READ_TIMEOUT_MS = 5_000;

    /** Client HTTP vers OSRM, avec délais bornés (timeouts contrôlés). */
    @Bean
    RestClient osrmRestClient(
            RestClient.Builder builder,
            @Value("${routing.osrm.base-url:https://router.project-osrm.org}") String baseUrl) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Bean
    RoutingService routingService(RouteProviderPort routeProvider) {
        return new RoutingService(routeProvider);
    }
}
