package com.urgencesante.emergencytaxonomy.internal.adapter.in.web;

import com.urgencesante.emergencytaxonomy.internal.adapter.in.web.dto.response.EmergencyCategoryResponse;
import com.urgencesante.emergencytaxonomy.internal.adapter.in.web.mapper.EmergencyTaxonomyWebMapper;
import com.urgencesante.emergencytaxonomy.internal.application.port.in.GetEmergencyTaxonomyUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST de la taxonomie. Conforme au contrat OpenAPI. Aucune
 * logique métier : mapping et appel du port entrant. Endpoint public, sans
 * paramètre — référentiel de navigation consommé par l'application patient.
 */
@RestController
@RequestMapping("/api/v1/emergency-taxonomy")
public class EmergencyTaxonomyController {

    private final GetEmergencyTaxonomyUseCase getEmergencyTaxonomy;
    private final EmergencyTaxonomyWebMapper mapper;

    public EmergencyTaxonomyController(
            GetEmergencyTaxonomyUseCase getEmergencyTaxonomy, EmergencyTaxonomyWebMapper mapper) {
        this.getEmergencyTaxonomy = getEmergencyTaxonomy;
        this.mapper = mapper;
    }

    @GetMapping
    public List<EmergencyCategoryResponse> taxonomy() {
        return getEmergencyTaxonomy.categories().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
