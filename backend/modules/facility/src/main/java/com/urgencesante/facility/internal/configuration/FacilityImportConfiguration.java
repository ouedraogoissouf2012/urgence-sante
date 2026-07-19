package com.urgencesante.facility.internal.configuration;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.application.port.out.KnownServicePort;
import com.urgencesante.facility.internal.application.service.FacilityImportService;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage de l'import d'annuaire. Le profil « production » est détecté pour
 * refuser toute donnée de démonstration à l'import.
 */
@Configuration
public class FacilityImportConfiguration {

    @Bean
    FacilityImportService facilityImportService(
            FacilityDirectoryPort directoryPort,
            KnownServicePort knownServicePort,
            Environment environment) {
        final boolean production = environment.acceptsProfiles(
                org.springframework.core.env.Profiles.of("production"));
        return new FacilityImportService(directoryPort, knownServicePort, production);
    }
}
