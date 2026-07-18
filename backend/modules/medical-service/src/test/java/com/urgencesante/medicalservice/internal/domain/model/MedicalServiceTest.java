package com.urgencesante.medicalservice.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.medicalservice.internal.domain.exception.MedicalServiceValidationException;
import org.junit.jupiter.api.Test;

class MedicalServiceTest {

    @Test
    void construit_un_service_valide_et_normalise() {
        final MedicalService service = MedicalService.of(
                MedicalServiceCode.of("  Maternity  "), "  Maternité  ", "  Emergency  ");

        assertThat(service.code().value()).isEqualTo("maternity");
        assertThat(service.label()).isEqualTo("Maternité");
        assertThat(service.category()).contains("emergency");
    }

    @Test
    void refuse_un_libelle_vide() {
        assertThatThrownBy(() -> MedicalService.of(MedicalServiceCode.of("x"), "  ", null))
                .isInstanceOf(MedicalServiceValidationException.class)
                .hasMessageContaining("libellé");
    }

    @Test
    void refuse_un_code_vide() {
        assertThatThrownBy(() -> MedicalServiceCode.of("  "))
                .isInstanceOf(MedicalServiceValidationException.class);
    }

    @Test
    void categorie_vide_traitee_comme_absente() {
        final MedicalService service = MedicalService.of(MedicalServiceCode.of("x"), "X", "   ");

        assertThat(service.category()).isEmpty();
    }

    @Test
    void l_egalite_repose_sur_le_code() {
        final MedicalService a = MedicalService.of(MedicalServiceCode.of("surgery"), "Chirurgie", null);
        final MedicalService b = MedicalService.of(MedicalServiceCode.of("SURGERY"), "Autre", "cat");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
