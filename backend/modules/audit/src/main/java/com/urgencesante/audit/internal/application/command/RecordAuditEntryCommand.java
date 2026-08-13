package com.urgencesante.audit.internal.application.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Commande d'enregistrement d'une ligne d'audit, construite par un adaptateur
 * d'entrée (ex. un écouteur d'événement) à partir d'un fait déjà survenu.
 *
 * @param sourceEventId identifiant de l'événement source (clé d'idempotence)
 * @param actorId nullable si l'acteur n'a pas pu être résolu à la source
 * @param actorLabel nullable, idem
 */
public record RecordAuditEntryCommand(
        UUID sourceEventId,
        String action,
        String resourceType,
        String resourceId,
        String actorId,
        String actorLabel,
        String correlationId,
        Instant occurredAt) {
}
