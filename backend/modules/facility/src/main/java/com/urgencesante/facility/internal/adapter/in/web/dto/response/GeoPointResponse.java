package com.urgencesante.facility.internal.adapter.in.web.dto.response;

/** Point géographique exposé par l'API (conforme au schéma OpenAPI GeoPoint). */
public record GeoPointResponse(double latitude, double longitude) {
}
