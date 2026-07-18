package com.urgencesante.medicalservice.internal.adapter.in.web.mapper;

import com.urgencesante.medicalservice.internal.adapter.in.web.dto.response.MedicalServiceResponse;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import org.springframework.stereotype.Component;

/** Traduit le domaine en DTO de réponse HTTP, sans logique métier. */
@Component
public class MedicalServiceWebMapper {

    public MedicalServiceResponse toResponse(MedicalService service) {
        return new MedicalServiceResponse(
                service.code().value(),
                service.label(),
                service.category().orElse(null));
    }
}
