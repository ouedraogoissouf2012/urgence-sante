package com.urgencesante.availability.internal.adapter.in.web.dto.response;

import java.time.Instant;

/** Entrée d'historique exposée (conforme au schéma OpenAPI AvailabilityHistoryEntry). */
public record AvailabilityHistoryEntryResponse(String status, Instant updatedAt) {
}
