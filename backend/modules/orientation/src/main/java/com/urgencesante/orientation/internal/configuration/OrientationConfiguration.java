package com.urgencesante.orientation.internal.configuration;

import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort;
import com.urgencesante.orientation.internal.application.port.out.CandidateFacilityPort;
import com.urgencesante.orientation.internal.application.port.out.ServiceCatalogPort;
import com.urgencesante.orientation.internal.application.port.out.TravelTimePort;
import com.urgencesante.orientation.internal.application.service.OrientationService;
import com.urgencesante.orientation.internal.domain.strategy.AvailabilityStrategy;
import com.urgencesante.orientation.internal.domain.strategy.OrientationStrategy;
import com.urgencesante.orientation.internal.domain.strategy.ProximityStrategy;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage Spring du module Orientation.
 *
 * <p>Les stratégies sont enregistrées ici : en ajouter une nouvelle se fait par
 * un nouveau bean, sans modifier le moteur (principe ouvert/fermé).
 */
@Configuration
public class OrientationConfiguration {

    @Bean
    OrientationStrategy availabilityStrategy() {
        return new AvailabilityStrategy();
    }

    @Bean
    OrientationStrategy proximityStrategy() {
        return new ProximityStrategy();
    }

    @Bean
    OrientationService orientationService(
            ServiceCatalogPort serviceCatalog,
            CandidateFacilityPort candidateFacilityPort,
            AvailabilityLookupPort availabilityLookupPort,
            TravelTimePort travelTimePort,
            List<OrientationStrategy> strategies) {
        return new OrientationService(
                serviceCatalog, candidateFacilityPort, availabilityLookupPort, travelTimePort, strategies);
    }
}
