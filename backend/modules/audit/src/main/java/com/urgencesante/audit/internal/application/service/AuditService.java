package com.urgencesante.audit.internal.application.service;

import com.urgencesante.audit.internal.application.command.RecordAuditEntryCommand;
import com.urgencesante.audit.internal.application.port.in.RecordAuditEntryUseCase;
import com.urgencesante.audit.internal.application.port.out.SaveAuditEntryPort;
import com.urgencesante.audit.internal.domain.model.AuditEntry;

/** Cas d'usage : traduire un fait déjà survenu en ligne d'audit persistée. */
public class AuditService implements RecordAuditEntryUseCase {

    private final SaveAuditEntryPort savePort;

    public AuditService(SaveAuditEntryPort savePort) {
        this.savePort = savePort;
    }

    @Override
    public void record(RecordAuditEntryCommand command) {
        final AuditEntry entry = AuditEntry.of(
                command.sourceEventId(),
                command.action(),
                command.resourceType(),
                command.resourceId(),
                command.actorId(),
                command.actorLabel(),
                command.correlationId(),
                command.occurredAt());
        savePort.saveIfAbsent(entry);
    }
}
