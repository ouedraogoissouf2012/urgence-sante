package com.urgencesante.audit.internal.application.port.in;

import com.urgencesante.audit.internal.application.command.RecordAuditEntryCommand;

/** Port entrant : enregistrer un fait déjà survenu dans la trace d'audit. */
public interface RecordAuditEntryUseCase {

    void record(RecordAuditEntryCommand command);
}
