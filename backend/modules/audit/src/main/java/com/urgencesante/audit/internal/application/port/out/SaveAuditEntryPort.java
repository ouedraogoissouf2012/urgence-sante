package com.urgencesante.audit.internal.application.port.out;

import com.urgencesante.audit.internal.domain.model.AuditEntry;

/**
 * Port sortant : persister une ligne d'audit.
 *
 * <p>{@code saveIfAbsent} nomme explicitement le contrat d'idempotence exigé
 * de l'implémentation : une ligne déjà connue (même {@code AuditEntry#id()})
 * est silencieusement ignorée, jamais écrasée ni dupliquée — la livraison au
 * moins une fois de l'outbox source peut représenter le même fait deux fois.
 */
public interface SaveAuditEntryPort {

    void saveIfAbsent(AuditEntry entry);
}
