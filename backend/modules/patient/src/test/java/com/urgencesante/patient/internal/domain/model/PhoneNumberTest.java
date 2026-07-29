package com.urgencesante.patient.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import org.junit.jupiter.api.Test;

class PhoneNumberTest {

    @Test
    void normalise_en_format_international_sans_espaces() {
        assertThat(PhoneNumber.of("+225 01 02 03 04 05").value()).isEqualTo("+2250102030405");
    }

    @Test
    void tolere_les_separateurs_et_espaces_multiples() {
        assertThat(PhoneNumber.of(" +225-01.02 03 04 05 ").value()).isEqualTo("+2250102030405");
    }

    @Test
    void deux_ecritures_du_meme_numero_sont_egales() {
        assertThat(PhoneNumber.of("+225 0102030405"))
                .isEqualTo(PhoneNumber.of("+2250102030405"));
    }

    @Test
    void refuse_un_numero_vide() {
        assertThatThrownBy(() -> PhoneNumber.of("  "))
                .isInstanceOf(PatientValidationException.class)
                .hasMessageContaining("téléphone");
    }

    @Test
    void refuse_un_numero_sans_indicatif_international() {
        assertThatThrownBy(() -> PhoneNumber.of("0102030405"))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void refuse_un_numero_trop_court() {
        assertThatThrownBy(() -> PhoneNumber.of("+22501"))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void refuse_un_numero_trop_long() {
        assertThatThrownBy(() -> PhoneNumber.of("+225010203040506070809"))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void refuse_des_lettres() {
        assertThatThrownBy(() -> PhoneNumber.of("+225ABCDEFGH"))
                .isInstanceOf(PatientValidationException.class);
    }
}
