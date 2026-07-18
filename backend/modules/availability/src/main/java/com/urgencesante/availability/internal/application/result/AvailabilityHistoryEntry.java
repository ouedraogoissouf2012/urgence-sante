package com.urgencesante.availability.internal.application.result;

import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import java.time.Instant;

/** Mise à jour passée du statut d'un service (historique auditable). */
public record AvailabilityHistoryEntry(AvailabilityStatus status, Instant updatedAt) {
}
