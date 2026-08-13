package com.urgencesante.audit.internal.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import com.urgencesante.audit.internal.application.command.RecordAuditEntryCommand;
import com.urgencesante.audit.internal.application.port.in.RecordAuditEntryUseCase;
import com.urgencesante.availability.AvailabilityUpdated;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvailabilityUpdatedAuditListenerTest {

    @Mock
    private RecordAuditEntryUseCase recordAuditEntry;

    private SimpleMeterRegistry meterRegistry;
    private AvailabilityUpdatedAuditListener listener;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        listener = new AvailabilityUpdatedAuditListener(recordAuditEntry, meterRegistry);
    }

    private static AvailabilityUpdated event() {
        final UUID id = UUID.randomUUID();
        return new AvailabilityUpdated(
                id, "corr-1", UUID.randomUUID(), "maternity", "LIMITED",
                Instant.parse("2026-01-01T12:00:00Z"), Instant.parse("2026-01-01T12:00:01Z"));
    }

    @Test
    void traduit_l_evenement_en_commande_sans_acteur_resolu() {
        final AvailabilityUpdated event = event();

        listener.onAvailabilityUpdated(event);

        final ArgumentCaptor<RecordAuditEntryCommand> captor =
                ArgumentCaptor.forClass(RecordAuditEntryCommand.class);
        then(recordAuditEntry).should().record(captor.capture());
        final RecordAuditEntryCommand command = captor.getValue();

        assertThat(command.sourceEventId()).isEqualTo(event.eventId());
        assertThat(command.action()).isEqualTo("AVAILABILITY_UPDATED");
        assertThat(command.resourceType()).isEqualTo("FACILITY_SERVICE_AVAILABILITY");
        assertThat(command.resourceId()).isEqualTo(event.facilityId() + ":maternity");
        assertThat(command.actorId()).isNull();
        assertThat(command.actorLabel()).isNull();
        assertThat(command.correlationId()).isEqualTo("corr-1");
        assertThat(command.occurredAt()).isEqualTo(event.occurredAt());
        assertThat(meterRegistry.counter("audit.record.errors").count()).isZero();
    }

    @Test
    void un_echec_d_enregistrement_est_absorbe_sans_propager_et_incremente_le_compteur() {
        doThrow(new RuntimeException("panne base")).when(recordAuditEntry).record(any());

        assertThatCode(() -> listener.onAvailabilityUpdated(event())).doesNotThrowAnyException();
        assertThat(meterRegistry.counter("audit.record.errors").count()).isEqualTo(1.0);
    }
}
