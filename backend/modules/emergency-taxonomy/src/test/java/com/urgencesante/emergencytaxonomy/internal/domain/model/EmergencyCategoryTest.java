package com.urgencesante.emergencytaxonomy.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class EmergencyCategoryTest {

    private static final List<Symptom> SYMPTOMS = List.of(Symptom.of("s1", "Symptôme"));
    private static final List<String> SERVICES = List.of("emergency");

    @Test
    void construit_une_categorie_valide() {
        final EmergencyCategory category = EmergencyCategory.of(
                "respiratoires", "Urgences respiratoires", 1, false, null, SYMPTOMS, SERVICES);

        assertThat(category.id()).isEqualTo("respiratoires");
        assertThat(category.directCallOnly()).isFalse();
        assertThat(category.directCallMessage()).isEmpty();
        assertThat(category.symptoms()).hasSize(1);
        assertThat(category.serviceCodes()).containsExactly("emergency");
    }

    @Test
    void une_categorie_a_appel_direct_porte_son_message() {
        final EmergencyCategory category = EmergencyCategory.of(
                "accidents", "Accidents", 4, true, "Appelez les secours.", SYMPTOMS, SERVICES);

        assertThat(category.directCallOnly()).isTrue();
        assertThat(category.directCallMessage()).contains("Appelez les secours.");
    }

    @Test
    void refuse_un_appel_direct_sans_message() {
        assertThatThrownBy(() -> EmergencyCategory.of(
                "accidents", "Accidents", 4, true, "  ", SYMPTOMS, SERVICES))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_un_identifiant_vide() {
        assertThatThrownBy(() -> EmergencyCategory.of(
                " ", "Libellé", 1, false, null, SYMPTOMS, SERVICES))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_une_categorie_sans_symptome() {
        assertThatThrownBy(() -> EmergencyCategory.of(
                "x", "Libellé", 1, false, null, List.of(), SERVICES))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_une_categorie_sans_service() {
        assertThatThrownBy(() -> EmergencyCategory.of(
                "x", "Libellé", 1, false, null, SYMPTOMS, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void l_identite_repose_sur_l_identifiant() {
        final EmergencyCategory a = EmergencyCategory.of("x", "A", 1, false, null, SYMPTOMS, SERVICES);
        final EmergencyCategory b = EmergencyCategory.of("x", "B", 2, false, null, SYMPTOMS, SERVICES);

        assertThat(a).isEqualTo(b);
        assertThat(a.symptoms()).as("liste immuable").isUnmodifiable();
    }
}
