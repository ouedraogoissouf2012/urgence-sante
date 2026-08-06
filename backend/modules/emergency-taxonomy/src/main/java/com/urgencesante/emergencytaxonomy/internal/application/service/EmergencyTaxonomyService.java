package com.urgencesante.emergencytaxonomy.internal.application.service;

import com.urgencesante.emergencytaxonomy.internal.application.port.in.GetEmergencyTaxonomyUseCase;
import com.urgencesante.emergencytaxonomy.internal.application.port.out.LoadEmergencyTaxonomyPort;
import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Cas d'usage de lecture de la taxonomie. Java pur. Garantit l'ordre d'affichage
 * (par {@code order}) quelle que soit l'ordre rendu par la source.
 */
public class EmergencyTaxonomyService implements GetEmergencyTaxonomyUseCase {

    private final LoadEmergencyTaxonomyPort loadEmergencyTaxonomyPort;

    public EmergencyTaxonomyService(LoadEmergencyTaxonomyPort loadEmergencyTaxonomyPort) {
        this.loadEmergencyTaxonomyPort = Objects.requireNonNull(loadEmergencyTaxonomyPort);
    }

    @Override
    public List<EmergencyCategory> categories() {
        return loadEmergencyTaxonomyPort.loadAll().stream()
                .sorted(Comparator.comparingInt(EmergencyCategory::order))
                .toList();
    }
}
