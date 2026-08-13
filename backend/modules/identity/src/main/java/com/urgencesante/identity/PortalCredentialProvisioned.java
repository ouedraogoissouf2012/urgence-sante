package com.urgencesante.identity;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement public : un nouveau credential portail a été provisionné.
 *
 * <p>Publié directement (sans outbox transactionnel — contrairement à
 * {@code AvailabilityUpdated} — car déclenché par une action CLI ponctuelle
 * et rare, pas par un flux HTTP à haut débit ; voir le runbook de
 * provisioning pour la justification complète) au moment où la transaction
 * qui persiste le credential COMMIT. {@code eventId} identifie l'événement de
 * façon stable pour la déduplication côté consommateur ; {@code occurredAt}
 * date le fait métier.
 */
public record PortalCredentialProvisioned(
        UUID eventId,
        UUID credentialId,
        String label,
        PortalRole role,
        UUID facilityId,
        Instant occurredAt) {
}
