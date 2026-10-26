package com.urgencesante.facility.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CareLevelTest {

    @Test
    void un_centre_de_premier_niveau_n_accueille_pas_les_urgences() {
        assertThat(CareLevel.PRIMARY.isEmergencyCapable()).isFalse();
    }

    @Test
    void les_centres_de_deuxieme_et_troisieme_niveau_accueillent_les_urgences() {
        assertThat(CareLevel.SECONDARY.isEmergencyCapable()).isTrue();
        assertThat(CareLevel.TERTIARY.isEmergencyCapable()).isTrue();
    }

    @Test
    void emergencyCapable_liste_exactement_les_niveaux_accueillant_les_urgences() {
        assertThat(CareLevel.emergencyCapable())
                .containsExactlyInAnyOrder(CareLevel.SECONDARY, CareLevel.TERTIARY);
    }

    @Test
    void emergencyCapable_ne_peut_pas_etre_modifie_par_un_appelant() {
        // Ensemble partagé : le modifier changerait la règle pour tout le monde.
        assertThatThrownBy(() -> CareLevel.emergencyCapable().add(CareLevel.PRIMARY))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void fromCode_reconnait_un_code_en_tolerant_casse_et_espaces() {
        assertThat(CareLevel.fromCode("TERTIARY")).contains(CareLevel.TERTIARY);
        assertThat(CareLevel.fromCode(" secondary ")).contains(CareLevel.SECONDARY);
    }

    @Test
    void fromCode_est_vide_pour_un_code_absent_ou_inconnu() {
        assertThat(CareLevel.fromCode(null)).isEmpty();
        assertThat(CareLevel.fromCode("   ")).isEmpty();
        assertThat(CareLevel.fromCode("QUATERNARY")).isEmpty();
        // Le niveau numérique du vocabulaire client n'est pas un code accepté.
        assertThat(CareLevel.fromCode("2")).isEmpty();
    }
}
