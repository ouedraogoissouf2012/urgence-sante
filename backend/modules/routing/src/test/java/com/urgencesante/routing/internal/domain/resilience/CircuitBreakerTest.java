package com.urgencesante.routing.internal.domain.resilience;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

    /** Horloge mutable pour piloter le temps dans les tests. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final CircuitBreaker breaker =
            new CircuitBreaker(3, Duration.ofSeconds(30), clock);

    @Test
    void ferme_par_defaut_et_reste_ferme_sous_le_seuil() {
        breaker.recordFailure();
        breaker.recordFailure();

        assertThat(breaker.allowRequest()).isTrue();
        assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void s_ouvre_au_seuil_et_refuse_les_appels() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();

        assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(breaker.allowRequest()).isFalse();
    }

    @Test
    void un_succes_remet_le_compteur_a_zero() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordSuccess();
        breaker.recordFailure();
        breaker.recordFailure();

        assertThat(breaker.allowRequest()).isTrue();
    }

    @Test
    void semi_ouvert_apres_le_delai_puis_reprise_sur_succes() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.allowRequest()).isFalse();

        clock.advance(Duration.ofSeconds(31));
        assertThat(breaker.allowRequest()).isTrue();
        assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        breaker.recordSuccess();
        assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void semi_ouvert_puis_echec_rouvre_immediatement() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        clock.advance(Duration.ofSeconds(31));
        assertThat(breaker.allowRequest()).isTrue();

        breaker.recordFailure();

        assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(breaker.allowRequest()).isFalse();
    }
}
