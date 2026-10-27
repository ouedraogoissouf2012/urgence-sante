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
    void accepte_chaque_prefixe_ivoirien_du_plan_2021() {
        for (final String prefix : new String[] {"01", "05", "07", "21", "25", "27"}) {
            assertThat(PhoneNumber.forNewAccount("+225 " + prefix + " 44 21 01 12").value())
                    .isEqualTo("+225" + prefix + "44210112");
        }
    }

    @Test
    void refuse_un_numero_ivoirien_qui_n_a_pas_10_chiffres() {
        // Accepté par l'ancienne règle générale (+ et 8 à 15 chiffres) alors
        // qu'aucun numéro ivoirien n'a 9 chiffres depuis 2021.
        assertThatThrownBy(() -> PhoneNumber.forNewAccount("+225544210112"))
                .isInstanceOf(PatientValidationException.class)
                .hasMessageContaining("ivoirien");
    }

    @Test
    void refuse_un_prefixe_ivoirien_impossible() {
        assertThatThrownBy(() -> PhoneNumber.forNewAccount("+2250944210112"))
                .isInstanceOf(PatientValidationException.class)
                .hasMessageContaining("01, 05, 07, 21, 25 ou 27");
    }

    @Test
    void un_numero_etranger_reste_soumis_a_la_seule_regle_generale() {
        assertThat(PhoneNumber.forNewAccount("+33 6 12 34 56 78").value()).isEqualTo("+33612345678");
        assertThat(PhoneNumber.forNewAccount("+226 70 12 34 56").value()).isEqualTo("+22670123456");
    }

    @Test
    void un_compte_cree_avant_la_regle_reste_chargeable_et_joignable() {
        // Seule la CRÉATION est durcie : relu depuis la base (sinon erreur 500)
        // et saisi à la connexion, un ancien numéro hors plan reste valide.
        assertThat(new PhoneNumber("+225544210112").value()).isEqualTo("+225544210112");
        assertThat(PhoneNumber.of("+225 544 210 112").value()).isEqualTo("+225544210112");
    }

    @Test
    void refuse_des_lettres() {
        assertThatThrownBy(() -> PhoneNumber.of("+225ABCDEFGH"))
                .isInstanceOf(PatientValidationException.class);
    }
}
