package com.urgencesante.patient.internal.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    // Force faible en test : BCrypt reste lent par nature, on ne teste pas la latence.
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void le_hash_ne_contient_pas_le_mot_de_passe_en_clair() {
        final String hash = hasher.hash("MonSecret2026");
        assertThat(hash).doesNotContain("MonSecret2026");
        assertThat(hash).startsWith("$2"); // signature BCrypt
    }

    @Test
    void matches_reconnait_le_bon_mot_de_passe() {
        final String hash = hasher.hash("MonSecret2026");
        assertThat(hasher.matches("MonSecret2026", hash)).isTrue();
    }

    @Test
    void matches_rejette_un_mauvais_mot_de_passe() {
        final String hash = hasher.hash("MonSecret2026");
        assertThat(hasher.matches("Faux", hash)).isFalse();
    }

    @Test
    void deux_hachages_du_meme_mot_de_passe_different_grace_au_sel() {
        assertThat(hasher.hash("MonSecret2026")).isNotEqualTo(hasher.hash("MonSecret2026"));
    }
}
