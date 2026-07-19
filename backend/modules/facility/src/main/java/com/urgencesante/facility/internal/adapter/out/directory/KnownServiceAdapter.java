package com.urgencesante.facility.internal.adapter.out.directory;

import com.urgencesante.facility.internal.application.port.out.KnownServicePort;
import com.urgencesante.medicalservice.MedicalServiceFacade;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** Valide un code de service via l'API publique du catalogue. */
@Component
class KnownServiceAdapter implements KnownServicePort {

    private final MedicalServiceFacade medicalServiceFacade;

    KnownServiceAdapter(MedicalServiceFacade medicalServiceFacade) {
        this.medicalServiceFacade = Objects.requireNonNull(medicalServiceFacade);
    }

    @Override
    public boolean isKnown(String serviceCode) {
        return medicalServiceFacade.exists(serviceCode);
    }
}
