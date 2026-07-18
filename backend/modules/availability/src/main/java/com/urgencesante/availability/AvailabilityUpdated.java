package com.urgencesante.availability;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement public : la disponibilité d'un service a été mise à jour. Publié à
 * destination des modules réactifs (audit, notification) sans exposer l'interne.
 */
public record AvailabilityUpdated(UUID facilityId, String serviceCode, String status, Instant updatedAt) {
}
