package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.medicalservice.MedicalServiceFacade;
import com.urgencesante.orientation.internal.application.port.out.ServiceCatalogPort;
import org.springframework.stereotype.Component;

/** Valide le service demandé via l'API publique du module Medical Service. */
@Component
class ServiceCatalogAdapter implements ServiceCatalogPort {

    private final MedicalServiceFacade medicalServiceFacade;

    ServiceCatalogAdapter(MedicalServiceFacade medicalServiceFacade) {
        this.medicalServiceFacade = medicalServiceFacade;
    }

    @Override
    public boolean exists(String serviceCode) {
        return medicalServiceFacade.exists(serviceCode);
    }
}
