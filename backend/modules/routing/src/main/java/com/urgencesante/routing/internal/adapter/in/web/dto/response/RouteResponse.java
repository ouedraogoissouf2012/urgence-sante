package com.urgencesante.routing.internal.adapter.in.web.dto.response;

/** Itinéraire exposé par l'API (conforme au schéma OpenAPI Route). */
public record RouteResponse(double distanceMeters, double durationSeconds) {
}
