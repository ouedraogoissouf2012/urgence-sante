package com.urgencesante.medicalservice.internal.adapter.out.persistence.mapper;

import com.urgencesante.medicalservice.internal.adapter.out.persistence.entity.MedicalServiceJpaEntity;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import com.urgencesante.medicalservice.internal.domain.model.MedicalServiceCode;
import org.springframework.stereotype.Component;

/** Traduit l'entité de persistance en agrégat du domaine. */
@Component
public class MedicalServiceEntityMapper {

    public MedicalService toDomain(MedicalServiceJpaEntity entity) {
        return MedicalService.of(
                MedicalServiceCode.of(entity.getCode()),
                entity.getLabel(),
                entity.getCategory());
    }
}
