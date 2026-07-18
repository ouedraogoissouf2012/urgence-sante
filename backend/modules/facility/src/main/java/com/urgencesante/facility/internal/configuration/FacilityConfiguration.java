package com.urgencesante.facility.internal.configuration;

import com.urgencesante.facility.internal.application.port.out.LoadFacilityPort;
import com.urgencesante.facility.internal.application.service.FacilityQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage Spring du module Facility. Le service applicatif reste en Java pur ;
 * il est exposé comme bean ici, câblé au port sortant fourni par l'adaptateur
 * de persistance.
 */
@Configuration
public class FacilityConfiguration {

    @Bean
    FacilityQueryService facilityQueryService(LoadFacilityPort loadFacilityPort) {
        return new FacilityQueryService(loadFacilityPort);
    }
}
