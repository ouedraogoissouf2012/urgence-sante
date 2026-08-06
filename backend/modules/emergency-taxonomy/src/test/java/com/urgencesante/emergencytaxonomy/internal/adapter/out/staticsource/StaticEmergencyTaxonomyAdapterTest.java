package com.urgencesante.emergencytaxonomy.internal.adapter.out.staticsource;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import com.urgencesante.emergencytaxonomy.internal.domain.model.Symptom;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verrouille la fidélité du référentiel statique au document client. Toute
 * dérive (catégorie manquante, flag d'appel direct erroné, symptôme sans code)
 * échoue ici, sans base de données.
 */
class StaticEmergencyTaxonomyAdapterTest {

    private final List<EmergencyCategory> categories = new StaticEmergencyTaxonomyAdapter().loadAll();

    @Test
    void fournit_les_douze_categories_du_document() {
        assertThat(categories).hasSize(12);
        assertThat(categories).extracting(EmergencyCategory::id).doesNotHaveDuplicates();
    }

    @Test
    void seules_accidents_et_intoxications_relevent_de_l_appel_direct() {
        final List<String> appelDirect = categories.stream()
                .filter(EmergencyCategory::directCallOnly)
                .map(EmergencyCategory::id)
                .toList();

        assertThat(appelDirect).containsExactlyInAnyOrder("accidents", "intoxications");
    }

    @Test
    void chaque_categorie_a_appel_direct_porte_un_message() {
        assertThat(categories)
                .filteredOn(EmergencyCategory::directCallOnly)
                .allSatisfy(category ->
                        assertThat(category.directCallMessage()).isPresent());
    }

    @Test
    void chaque_categorie_a_au_moins_un_symptome_et_un_service() {
        assertThat(categories).allSatisfy(category -> {
            assertThat(category.symptoms()).as("symptômes de " + category.id()).isNotEmpty();
            assertThat(category.serviceCodes()).as("services de " + category.id()).isNotEmpty();
        });
    }

    @Test
    void les_identifiants_de_symptomes_sont_uniques_dans_chaque_categorie() {
        assertThat(categories).allSatisfy(category ->
                assertThat(category.symptoms())
                        .extracting(Symptom::id)
                        .as("symptômes de " + category.id())
                        .doesNotHaveDuplicates());
    }

    @Test
    void les_ordres_couvrent_un_a_douze_sans_trou() {
        assertThat(categories)
                .extracting(EmergencyCategory::order)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    }

    @Test
    void mappe_correctement_une_categorie_de_reference() {
        final EmergencyCategory respiratoires = categories.stream()
                .filter(category -> category.id().equals("respiratoires"))
                .findFirst()
                .orElseThrow();

        assertThat(respiratoires.serviceCodes())
                .containsExactly("pulmonology", "intensive_care", "emergency");
        assertThat(respiratoires.symptoms()).extracting(Symptom::id).contains("crise-asthme");
    }
}
