package com.urgencesante.routing.internal.adapter.out.osrm;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.routing.internal.domain.resilience.CircuitBreaker;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class OsrmHealthIndicatorTest {

    private final CircuitBreaker breaker =
            new CircuitBreaker(1, Duration.ofSeconds(30), Clock.systemUTC());
    private final OsrmHealthIndicator indicator = new OsrmHealthIndicator(breaker);

    @Test
    void up_quand_le_circuit_est_ferme() {
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void degrade_quand_le_circuit_est_ouvert_sans_bloquer_la_readiness() {
        breaker.recordFailure();

        assertThat(indicator.health().getStatus()).isEqualTo(OsrmHealthIndicator.DEGRADED);
        assertThat(indicator.health().getDetails()).containsEntry("circuit", "OPEN");
    }
}
