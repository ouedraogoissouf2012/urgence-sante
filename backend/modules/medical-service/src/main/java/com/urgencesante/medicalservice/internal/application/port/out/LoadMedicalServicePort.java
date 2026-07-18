package com.urgencesante.medicalservice.internal.application.port.out;

import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import com.urgencesante.medicalservice.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Optional;

/** Port sortant : lecture du catalogue des services médicaux. */
public interface LoadMedicalServicePort {

    /** Retourne le catalogue, filtré par catégorie si fournie, trié par libellé. */
    List<MedicalService> findAll(Optional<String> category);

    Optional<MedicalService> findByCode(MedicalServiceCode code);
}
