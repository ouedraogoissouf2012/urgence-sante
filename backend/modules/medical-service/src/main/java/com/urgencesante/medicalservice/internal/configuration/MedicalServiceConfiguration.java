package com.urgencesante.medicalservice.internal.configuration;

import com.urgencesante.medicalservice.internal.application.port.out.LoadMedicalServicePort;
import com.urgencesante.medicalservice.internal.application.service.MedicalServiceCatalogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Assemblage Spring du module Medical Service. */
@Configuration
public class MedicalServiceConfiguration {

    @Bean
    MedicalServiceCatalogService medicalServiceCatalogService(LoadMedicalServicePort loadMedicalServicePort) {
        return new MedicalServiceCatalogService(loadMedicalServicePort);
    }
}
