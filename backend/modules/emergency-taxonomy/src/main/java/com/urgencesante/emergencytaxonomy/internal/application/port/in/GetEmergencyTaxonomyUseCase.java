package com.urgencesante.emergencytaxonomy.internal.application.port.in;

import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import java.util.List;

/** Port entrant : obtenir la taxonomie des urgences (catégories ordonnées). */
public interface GetEmergencyTaxonomyUseCase {

    List<EmergencyCategory> categories();
}
