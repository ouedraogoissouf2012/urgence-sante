package com.urgencesante.availability.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.out.AvailabilityEventPublisher;
import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import com.urgencesante.availability.internal.domain.model.Availability;
import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import com.urgencesante.availability.internal.domain.model.Freshness;
import com.urgencesante.availability.internal.domain.policy.FreshnessPolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {

    private static final UUID FACILITY = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final List<Availability> saved = new ArrayList<>();
    private final List<Availability> stored = new ArrayList<>();
    private final List<AvailabilityUpdated> published = new ArrayList<>();

    private final SaveAvailabilityPort savePort = saved::add;
    private final LoadAvailabilityPort loadPort = facilityId -> stored;
    private final AvailabilityEventPublisher publisher = published::add;

    private final AvailabilityService service = new AvailabilityService(
            savePort, loadPort, publisher,
            Clock.fixed(NOW, ZoneOffset.UTC), FreshnessPolicy.defaults());

    @Test
    void la_mise_a_jour_persiste_publie_et_horodate() {
        final ServiceAvailabilitySnapshot snapshot = service.update(
                new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED));

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).status()).isEqualTo(AvailabilityStatus.LIMITED);
        assertThat(saved.get(0).updatedAt()).isEqualTo(NOW);
        assertThat(snapshot.freshness()).isEqualTo(Freshness.FRESH);

        assertThat(published).hasSize(1);
        assertThat(published.get(0).status()).isEqualTo("LIMITED");
        assertThat(published.get(0).facilityId()).isEqualTo(FACILITY);
    }

    @Test
    void la_consultation_calcule_la_fraicheur_avec_l_horloge() {
        stored.add(Availability.of(
                FACILITY, "surgery", AvailabilityStatus.AVAILABLE, NOW.minus(Duration.ofHours(2))));

        final FacilityAvailabilitySnapshot result = service.forFacility(FACILITY);

        assertThat(result.services()).hasSize(1);
        assertThat(result.services().get(0).freshness()).isEqualTo(Freshness.STALE);
    }
}
