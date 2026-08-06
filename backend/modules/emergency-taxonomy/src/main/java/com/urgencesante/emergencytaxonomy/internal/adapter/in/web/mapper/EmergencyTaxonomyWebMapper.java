package com.urgencesante.emergencytaxonomy.internal.adapter.in.web.mapper;

import com.urgencesante.emergencytaxonomy.internal.adapter.in.web.dto.response.EmergencyCategoryResponse;
import com.urgencesante.emergencytaxonomy.internal.adapter.in.web.dto.response.SymptomResponse;
import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import org.springframework.stereotype.Component;

/** Traduit le domaine en DTO de réponse HTTP, sans logique métier. */
@Component
public class EmergencyTaxonomyWebMapper {

    public EmergencyCategoryResponse toResponse(EmergencyCategory category) {
        return new EmergencyCategoryResponse(
                category.id(),
                category.label(),
                category.directCallOnly(),
                category.directCallMessage().orElse(null),
                category.symptoms().stream()
                        .map(symptom -> new SymptomResponse(symptom.id(), symptom.label()))
                        .toList(),
                category.serviceCodes());
    }
}
