package com.urgencesante.availability.internal.adapter.in.web.dto.request;

import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;

/** Corps de requête de mise à jour de statut (conforme au schéma OpenAPI). */
public record UpdateAvailabilityRequest(AvailabilityStatus status) {
}
