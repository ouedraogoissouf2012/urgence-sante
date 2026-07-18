package com.urgencesante.medicalservice.internal.application.service;

import com.urgencesante.medicalservice.internal.application.port.in.ListMedicalServicesUseCase;
import com.urgencesante.medicalservice.internal.application.port.out.LoadMedicalServicePort;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Cas d'usage de lecture du catalogue. Java pur ; la dépendance sur le port
 * sortant est injectée par constructeur.
 */
public class MedicalServiceCatalogService implements ListMedicalServicesUseCase {

    private final LoadMedicalServicePort loadMedicalServicePort;

    public MedicalServiceCatalogService(LoadMedicalServicePort loadMedicalServicePort) {
        this.loadMedicalServicePort = Objects.requireNonNull(loadMedicalServicePort);
    }

    @Override
    public List<MedicalService> list(Optional<String> category) {
        return loadMedicalServicePort.findAll(category == null ? Optional.empty() : category);
    }
}
