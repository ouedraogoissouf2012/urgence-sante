package com.urgencesante.medicalservice.internal;

import com.urgencesante.medicalservice.MedicalServiceFacade;
import com.urgencesante.medicalservice.MedicalServiceView;
import com.urgencesante.medicalservice.internal.application.port.out.LoadMedicalServicePort;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import com.urgencesante.medicalservice.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module en s'appuyant sur le port de lecture. */
@Component
class MedicalServiceFacadeAdapter implements MedicalServiceFacade {

    private final LoadMedicalServicePort loadMedicalServicePort;

    MedicalServiceFacadeAdapter(LoadMedicalServicePort loadMedicalServicePort) {
        this.loadMedicalServicePort = loadMedicalServicePort;
    }

    @Override
    public Optional<MedicalServiceView> findByCode(String code) {
        return loadMedicalServicePort.findByCode(MedicalServiceCode.of(code))
                .map(MedicalServiceFacadeAdapter::toView);
    }

    @Override
    public boolean exists(String code) {
        return findByCode(code).isPresent();
    }

    @Override
    public List<MedicalServiceView> all() {
        return loadMedicalServicePort.findAll(Optional.empty()).stream()
                .map(MedicalServiceFacadeAdapter::toView)
                .toList();
    }

    private static MedicalServiceView toView(MedicalService service) {
        return new MedicalServiceView(service.code().value(), service.label(), service.category());
    }
}
