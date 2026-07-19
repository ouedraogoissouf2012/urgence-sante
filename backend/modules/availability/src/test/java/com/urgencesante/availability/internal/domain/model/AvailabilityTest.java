package com.urgencesante.availability.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.availability.internal.domain.exception.AvailabilityValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AvailabilityTest {

    private static final UUID FACILITY = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void normalise_le_code_de_service() {
        final Availability availability =
                Availability.of(FACILITY, "  MATERNITY  ", AvailabilityStatus.AVAILABLE, NOW);

        assertThat(availability.serviceCode()).isEqualTo("maternity");
    }

    @Test
    void refuse_un_code_vide_ou_trop_long() {
        assertThatThrownBy(() -> Availability.of(FACILITY, "  ", AvailabilityStatus.AVAILABLE, NOW))
                .isInstanceOf(AvailabilityValidationException.class);
        assertThatThrownBy(() -> Availability.of(
                FACILITY, "x".repeat(65), AvailabilityStatus.AVAILABLE, NOW))
                .isInstanceOf(AvailabilityValidationException.class)
                .hasMessageContaining("64");
    }
}
