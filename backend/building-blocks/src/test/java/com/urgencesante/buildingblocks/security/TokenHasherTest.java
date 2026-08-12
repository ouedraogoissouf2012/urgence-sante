package com.urgencesante.buildingblocks.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHasherTest {

    @Test
    void produit_une_empreinte_hexadecimale_de_64_caracteres() {
        final String hash = TokenHasher.sha256Hex("un-jeton-quelconque");

        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void est_deterministe_pour_un_meme_jeton() {
        assertThat(TokenHasher.sha256Hex("meme-jeton"))
                .isEqualTo(TokenHasher.sha256Hex("meme-jeton"));
    }

    @Test
    void produit_des_empreintes_differentes_pour_des_jetons_differents() {
        assertThat(TokenHasher.sha256Hex("jeton-a"))
                .isNotEqualTo(TokenHasher.sha256Hex("jeton-b"));
    }
}
