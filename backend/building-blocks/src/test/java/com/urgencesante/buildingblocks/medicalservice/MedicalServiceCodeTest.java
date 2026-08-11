package com.urgencesante.buildingblocks.medicalservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MedicalServiceCodeTest {

    @Test
    void normalise_en_minuscules_et_supprime_les_espaces() {
        assertThat(MedicalServiceCode.of("  Maternity  ").value()).isEqualTo("maternity");
    }

    @Test
    void refuse_une_valeur_nulle() {
        assertThatThrownBy(() -> MedicalServiceCode.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_une_valeur_vide() {
        assertThatThrownBy(() -> MedicalServiceCode.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_un_code_trop_long() {
        assertThatThrownBy(() -> MedicalServiceCode.of("x".repeat(65)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64");
    }

    @Test
    void accepte_la_longueur_maximale() {
        assertThat(MedicalServiceCode.of("x".repeat(MedicalServiceCode.MAX_LENGTH)).value())
                .hasSize(MedicalServiceCode.MAX_LENGTH);
    }

    @Test
    void l_egalite_repose_sur_la_valeur_normalisee() {
        assertThat(MedicalServiceCode.of("surgery")).isEqualTo(MedicalServiceCode.of("SURGERY"));
    }
}
