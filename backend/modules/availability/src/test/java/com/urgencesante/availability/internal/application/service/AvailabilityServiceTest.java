package com.urgencesante.availability.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.OfferedServicePort;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.TransactionPort;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import com.urgencesante.availability.internal.domain.exception.ServiceNotOfferedException;
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
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {

    private static final UUID FACILITY = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final List<String> operations = new ArrayList<>();
    private final List<Availability> saved = new ArrayList<>();
    private final List<Availability> stored = new ArrayList<>();
    private final List<AvailabilityUpdated> outbox = new ArrayList<>();

    private final SaveAvailabilityPort savePort = availability -> {
        operations.add("save");
        saved.add(availability);
    };
    private final LoadAvailabilityPort loadPort = new LoadAvailabilityPort() {
        @Override
        public List<Availability> findByFacility(UUID facilityId) {
            return stored;
        }

        @Override
        public List<Availability> history(UUID facilityId, String serviceCode, int limit) {
            return stored.stream().limit(limit).toList();
        }
    };

    private boolean serviceOffered = true;
    private final OfferedServicePort offeredPort = (facilityId, serviceCode) -> serviceOffered;

    private final OutboxPort outboxPort = new OutboxPort() {
        @Override
        public void append(AvailabilityUpdated event) {
            operations.add("outbox");
            outbox.add(event);
        }

        @Override
        public List<AvailabilityUpdated> unpublished(int limit) {
            return List.copyOf(outbox);
        }

        @Override
        public void markPublished(UUID eventId) {
            // sans objet pour ces tests
        }

        @Override
        public void recordFailure(UUID eventId) {
            // sans objet pour ces tests
        }
    };

    /** Fausse frontière transactionnelle : trace l'ouverture/fermeture. */
    private final TransactionPort transactionPort = new TransactionPort() {
        @Override
        public <T> T inTransaction(Supplier<T> work) {
            operations.add("tx-begin");
            final T result = work.get();
            operations.add("tx-commit");
            return result;
        }
    };

    private final AvailabilityService service = new AvailabilityService(
            savePort, loadPort, offeredPort, outboxPort, transactionPort,
            Clock.fixed(NOW, ZoneOffset.UTC), FreshnessPolicy.defaults());

    @Test
    void persistance_et_outbox_sont_atomiques_dans_la_transaction_du_cas_d_usage() {
        final ServiceAvailabilitySnapshot snapshot = service.update(
                new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED));

        // Ordre prouvé : begin → save → outbox → commit (tout ou rien).
        assertThat(operations).containsExactly("tx-begin", "save", "outbox", "tx-commit");
        assertThat(saved.get(0).updatedAt()).isEqualTo(NOW);
        assertThat(snapshot.freshness()).isEqualTo(Freshness.FRESH);
    }

    @Test
    void l_evenement_porte_identifiant_correlation_et_dates() {
        service.update(new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED));

        final AvailabilityUpdated event = outbox.get(0);
        assertThat(event.eventId()).isNotNull();
        assertThat(event.correlationId()).isNotBlank();
        assertThat(event.status()).isEqualTo("LIMITED");
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.facilityId()).isEqualTo(FACILITY);
    }

    @Test
    void refuse_un_service_non_offert_sans_rien_ecrire() {
        serviceOffered = false;

        assertThatThrownBy(() -> service.update(
                new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED)))
                .isInstanceOf(ServiceNotOfferedException.class);

        assertThat(saved).isEmpty();
        assertThat(outbox).isEmpty();
        assertThat(operations).isEmpty();
    }

    @Test
    void l_historique_restitue_les_statuts_passes() {
        stored.add(Availability.of(
                FACILITY, "maternity", AvailabilityStatus.LIMITED, NOW.minus(Duration.ofMinutes(5))));

        final var history = service.history(FACILITY, "maternity", 10);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).status()).isEqualTo(AvailabilityStatus.LIMITED);
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
