package com.urgencesante.availability;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement public : la disponibilité d'un service a été mise à jour.
 *
 * <p>Publié via un OUTBOX transactionnel avec livraison AU MOINS UNE FOIS :
 * {@code eventId} identifie l'événement de façon stable et permet aux
 * consommateurs d'être idempotents (déduplication) ; {@code correlationId}
 * relie l'événement à la requête d'origine ; {@code occurredAt} date le fait
 * métier.
 */
public record AvailabilityUpdated(
        UUID eventId,
        String correlationId,
        UUID facilityId,
        String serviceCode,
        String status,
        Instant updatedAt,
        Instant occurredAt) {
}
