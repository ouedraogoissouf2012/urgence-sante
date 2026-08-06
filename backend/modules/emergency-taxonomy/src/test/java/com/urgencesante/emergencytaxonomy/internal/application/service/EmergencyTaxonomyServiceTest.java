package com.urgencesante.emergencytaxonomy.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.emergencytaxonomy.internal.application.port.out.LoadEmergencyTaxonomyPort;
import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import com.urgencesante.emergencytaxonomy.internal.domain.model.Symptom;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmergencyTaxonomyServiceTest {

    private static EmergencyCategory category(String id, int order) {
        return EmergencyCategory.of(
                id, id, order, false, null, List.of(Symptom.of("s", "S")), List.of("emergency"));
    }

    @Test
    void retourne_les_categories_ordonnees_par_ordre() {
        // Le port rend volontairement les catégories dans le désordre.
        final LoadEmergencyTaxonomyPort desordre =
                () -> List.of(category("c", 3), category("a", 1), category("b", 2));
        final EmergencyTaxonomyService service = new EmergencyTaxonomyService(desordre);

        final List<EmergencyCategory> result = service.categories();

        assertThat(result).extracting(EmergencyCategory::id).containsExactly("a", "b", "c");
    }
}
