package com.urgencesante.emergencytaxonomy.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SymptomTest {

    @Test
    void construit_un_symptome_valide_en_nettoyant_les_espaces() {
        final Symptom symptom = Symptom.of("  crise-asthme  ", "  Crise d'asthme  ");

        assertThat(symptom.id()).isEqualTo("crise-asthme");
        assertThat(symptom.label()).isEqualTo("Crise d'asthme");
    }

    @Test
    void refuse_un_identifiant_vide() {
        assertThatThrownBy(() -> Symptom.of(" ", "Libellé"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_un_libelle_vide() {
        assertThatThrownBy(() -> Symptom.of("id", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
