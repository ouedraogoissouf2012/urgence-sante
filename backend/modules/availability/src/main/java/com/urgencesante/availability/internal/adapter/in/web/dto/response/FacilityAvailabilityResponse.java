package com.urgencesante.availability.internal.adapter.in.web.dto.response;

import java.util.List;

/** Disponibilité d'un établissement exposée (conforme au schéma OpenAPI FacilityAvailability). */
public record FacilityAvailabilityResponse(String facilityId, List<ServiceAvailabilityResponse> services) {
}
