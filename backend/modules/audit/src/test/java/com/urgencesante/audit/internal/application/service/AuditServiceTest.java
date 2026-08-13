package com.urgencesante.audit.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.audit.internal.application.command.RecordAuditEntryCommand;
import com.urgencesante.audit.internal.application.port.out.SaveAuditEntryPort;
import com.urgencesante.audit.internal.domain.model.AuditEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditServiceTest {

    /** Faux port en mémoire — capture les entrées transmises pour assertion. */
    private static final class FakeSavePort implements SaveAuditEntryPort {
        final List<AuditEntry> saved = new ArrayList<>();

        @Override
        public void saveIfAbsent(AuditEntry entry) {
            saved.add(entry);
        }
    }

    private final FakeSavePort savePort = new FakeSavePort();
    private final AuditService service = new AuditService(savePort);

    @Test
    void traduit_la_commande_en_ligne_d_audit_et_la_transmet_au_port() {
        final UUID sourceEventId = UUID.randomUUID();
        final Instant when = Instant.parse("2026-01-01T12:00:00Z");

        service.record(new RecordAuditEntryCommand(
                sourceEventId, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY",
                "f1:maternity", null, null, "corr-1", when));

        assertThat(savePort.saved).hasSize(1);
        final AuditEntry entry = savePort.saved.get(0);
        assertThat(entry.id()).isEqualTo(sourceEventId);
        assertThat(entry.action()).isEqualTo("AVAILABILITY_UPDATED");
        assertThat(entry.resourceId()).isEqualTo("f1:maternity");
        assertThat(entry.correlationId()).isEqualTo("corr-1");
        assertThat(entry.occurredAt()).isEqualTo(when);
    }
}
