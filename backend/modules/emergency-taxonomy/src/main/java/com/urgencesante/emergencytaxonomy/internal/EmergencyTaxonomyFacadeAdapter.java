package com.urgencesante.emergencytaxonomy.internal;

import com.urgencesante.emergencytaxonomy.EmergencyCategoryView;
import com.urgencesante.emergencytaxonomy.EmergencyTaxonomyFacade;
import com.urgencesante.emergencytaxonomy.SymptomView;
import com.urgencesante.emergencytaxonomy.internal.application.port.in.GetEmergencyTaxonomyUseCase;
import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import java.util.List;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module en s'appuyant sur le port entrant. */
@Component
class EmergencyTaxonomyFacadeAdapter implements EmergencyTaxonomyFacade {

    private final GetEmergencyTaxonomyUseCase getEmergencyTaxonomy;

    EmergencyTaxonomyFacadeAdapter(GetEmergencyTaxonomyUseCase getEmergencyTaxonomy) {
        this.getEmergencyTaxonomy = getEmergencyTaxonomy;
    }

    @Override
    public List<EmergencyCategoryView> categories() {
        return getEmergencyTaxonomy.categories().stream()
                .map(EmergencyTaxonomyFacadeAdapter::toView)
                .toList();
    }

    private static EmergencyCategoryView toView(EmergencyCategory category) {
        return new EmergencyCategoryView(
                category.id(),
                category.label(),
                category.directCallOnly(),
                category.directCallMessage(),
                category.symptoms().stream()
                        .map(symptom -> new SymptomView(symptom.id(), symptom.label()))
                        .toList(),
                category.serviceCodes());
    }
}
