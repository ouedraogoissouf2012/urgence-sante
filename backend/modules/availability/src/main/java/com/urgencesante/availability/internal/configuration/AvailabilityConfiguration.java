package com.urgencesante.availability.internal.configuration;

import com.urgencesante.availability.internal.application.port.out.AvailabilityEventPublisher;
import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.OfferedServicePort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.application.service.AvailabilityService;
import com.urgencesante.availability.internal.domain.policy.FreshnessPolicy;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Assemblage Spring du module Availability. */
@Configuration
public class AvailabilityConfiguration {

    /** Horloge injectable (fournie une seule fois pour l'application). */
    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    FreshnessPolicy freshnessPolicy() {
        return FreshnessPolicy.defaults();
    }

    @Bean
    AvailabilityService availabilityService(
            SaveAvailabilityPort saveAvailabilityPort,
            LoadAvailabilityPort loadAvailabilityPort,
            OfferedServicePort offeredServicePort,
            AvailabilityEventPublisher eventPublisher,
            Clock clock,
            FreshnessPolicy freshnessPolicy) {
        return new AvailabilityService(
                saveAvailabilityPort, loadAvailabilityPort, offeredServicePort,
                eventPublisher, clock, freshnessPolicy);
    }
}
