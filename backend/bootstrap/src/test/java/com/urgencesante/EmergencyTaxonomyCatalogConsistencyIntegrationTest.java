package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.emergencytaxonomy.EmergencyTaxonomyFacade;
import com.urgencesante.medicalservice.MedicalServiceFacade;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Invariant de cohérence entre modules : chaque service « recherché » référencé
 * par la taxonomie des urgences doit exister dans le catalogue Medical Service
 * (migrations V3 + V11). Verrouille l'alignement taxonomie ⇄ catalogue.
 */
@SpringBootTest
@ActiveProfiles("test")
class EmergencyTaxonomyCatalogConsistencyIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private EmergencyTaxonomyFacade taxonomy;

    @Autowired
    private MedicalServiceFacade catalog;

    @Test
    void chaque_service_reference_par_la_taxonomie_existe_au_catalogue() {
        final List<String> inconnus = taxonomy.categories().stream()
                .flatMap(category -> category.serviceCodes().stream())
                .distinct()
                .filter(code -> !catalog.exists(code))
                .toList();

        assertThat(inconnus)
                .as("codes de services référencés par la taxonomie mais absents du catalogue")
                .isEmpty();
    }
}
