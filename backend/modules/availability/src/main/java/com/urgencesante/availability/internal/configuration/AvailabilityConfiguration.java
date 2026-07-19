package com.urgencesante.availability.internal.configuration;

import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.OfferedServicePort;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.TransactionPort;
import com.urgencesante.availability.internal.application.service.AvailabilityService;
import com.urgencesante.availability.internal.domain.policy.FreshnessPolicy;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage Spring du module Availability. L'horloge applicative est fournie
 * par l'assemblage global (bootstrap).
 */
@Configuration
public class AvailabilityConfiguration {

    @Bean
    FreshnessPolicy freshnessPolicy() {
        return FreshnessPolicy.defaults();
    }

    @Bean
    AvailabilityService availabilityService(
            SaveAvailabilityPort saveAvailabilityPort,
            LoadAvailabilityPort loadAvailabilityPort,
            OfferedServicePort offeredServicePort,
            OutboxPort outboxPort,
            TransactionPort transactionPort,
            Clock clock,
            FreshnessPolicy freshnessPolicy) {
        return new AvailabilityService(
                saveAvailabilityPort, loadAvailabilityPort, offeredServicePort,
                outboxPort, transactionPort, clock, freshnessPolicy);
    }
}
