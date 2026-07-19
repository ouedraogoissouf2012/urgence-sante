package com.urgencesante.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private final java.time.Clock clock = new java.time.Clock() {
        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public java.time.Clock withZone(java.time.ZoneId zone) { return this; }
    };

    @Test
    void autorise_jusqu_a_la_capacite_puis_refuse() {
        final RateLimiter limiter = new RateLimiter(3, Duration.ofMinutes(1), clock);

        assertThat(limiter.tryAcquire("k")).isTrue();
        assertThat(limiter.tryAcquire("k")).isTrue();
        assertThat(limiter.tryAcquire("k")).isTrue();
        assertThat(limiter.tryAcquire("k")).as("4e refusée").isFalse();
    }

    @Test
    void les_cles_sont_independantes() {
        final RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1), clock);

        assertThat(limiter.tryAcquire("a")).isTrue();
        assertThat(limiter.tryAcquire("b")).as("autre clé, autre seau").isTrue();
        assertThat(limiter.tryAcquire("a")).isFalse();
    }

    @Test
    void les_jetons_se_reconstituent_avec_le_temps() {
        final RateLimiter limiter = new RateLimiter(2, Duration.ofMinutes(1), clock);
        limiter.tryAcquire("k");
        limiter.tryAcquire("k");
        assertThat(limiter.tryAcquire("k")).isFalse();

        now = now.plus(Duration.ofSeconds(31)); // ~1 jeton reconstitué

        assertThat(limiter.tryAcquire("k")).isTrue();
    }
}
