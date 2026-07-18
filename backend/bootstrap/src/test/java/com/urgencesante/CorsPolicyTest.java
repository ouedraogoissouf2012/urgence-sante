package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CorsPolicyTest {

    @Test
    void aucune_origine_par_defaut() {
        final CorsPolicy policy = CorsPolicy.of("", false);

        assertThat(policy.isEnabled()).isFalse();
        assertThat(policy.allowedOriginPatterns()).isEmpty();
    }

    @Test
    void liste_separee_par_virgules_nettoyee() {
        final CorsPolicy policy =
                CorsPolicy.of(" http://localhost:5173 , http://localhost:8081 ,, ", false);

        assertThat(policy.allowedOriginPatterns())
                .containsExactly("http://localhost:5173", "http://localhost:8081");
    }

    @Test
    void motif_generique_tolere_hors_production() {
        final CorsPolicy policy = CorsPolicy.of("http://localhost:*", false);

        assertThat(policy.isEnabled()).isTrue();
    }

    @Test
    void motif_generique_refuse_en_production() {
        assertThatThrownBy(() -> CorsPolicy.of("http://localhost:*", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("production");

        assertThatThrownBy(() -> CorsPolicy.of("*", true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void origines_explicites_acceptees_en_production() {
        final CorsPolicy policy = CorsPolicy.of("https://portail.urgence-sante.ci", true);

        assertThat(policy.allowedOriginPatterns())
                .containsExactly("https://portail.urgence-sante.ci");
    }
}
