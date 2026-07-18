package com.urgencesante.availability.internal.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.availability.internal.domain.model.Freshness;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FreshnessPolicyTest {

    private final FreshnessPolicy policy = FreshnessPolicy.defaults(); // 15 min / 60 min
    private final Instant now = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void recent_est_frais() {
        assertThat(policy.evaluate(now.minus(Duration.ofMinutes(5)), now)).isEqualTo(Freshness.FRESH);
    }

    @Test
    void borne_frais_incluse() {
        assertThat(policy.evaluate(now.minus(Duration.ofMinutes(15)), now)).isEqualTo(Freshness.FRESH);
    }

    @Test
    void intermediaire_est_vieillissant() {
        assertThat(policy.evaluate(now.minus(Duration.ofMinutes(30)), now)).isEqualTo(Freshness.AGING);
    }

    @Test
    void ancien_est_perime() {
        assertThat(policy.evaluate(now.minus(Duration.ofHours(2)), now)).isEqualTo(Freshness.STALE);
    }

    @Test
    void horodatage_futur_traite_comme_frais() {
        assertThat(policy.evaluate(now.plus(Duration.ofMinutes(5)), now)).isEqualTo(Freshness.FRESH);
    }

    @Test
    void refuse_une_configuration_incoherente() {
        assertThatThrownBy(() -> new FreshnessPolicy(Duration.ofMinutes(60), Duration.ofMinutes(15)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
