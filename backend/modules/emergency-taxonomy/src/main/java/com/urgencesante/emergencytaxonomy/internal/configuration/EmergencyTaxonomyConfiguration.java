package com.urgencesante.emergencytaxonomy.internal.configuration;

import com.urgencesante.emergencytaxonomy.internal.application.port.out.LoadEmergencyTaxonomyPort;
import com.urgencesante.emergencytaxonomy.internal.application.service.EmergencyTaxonomyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Assemblage Spring du module Emergency Taxonomy. */
@Configuration
public class EmergencyTaxonomyConfiguration {

    @Bean
    EmergencyTaxonomyService emergencyTaxonomyService(LoadEmergencyTaxonomyPort loadEmergencyTaxonomyPort) {
        return new EmergencyTaxonomyService(loadEmergencyTaxonomyPort);
    }
}
