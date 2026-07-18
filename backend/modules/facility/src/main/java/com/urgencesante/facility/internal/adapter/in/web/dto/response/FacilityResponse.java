package com.urgencesante.facility.internal.adapter.in.web.dto.response;

import java.util.List;

/**
 * Établissement exposé par l'API (conforme au schéma OpenAPI Facility).
 *
 * @param distanceMeters présent uniquement pour une recherche par proximité
 */
public record FacilityResponse(
        String id,
        String name,
        GeoPointResponse location,
        String phone,
        List<String> services,
        Integer distanceMeters) {
}
