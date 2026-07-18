package com.urgencesante.routing.internal.domain.model;

import com.urgencesante.routing.internal.domain.exception.RoutingValidationException;

/** Coordonnées géographiques (WGS84). Value object auto-validé. */
public record Coordinates(double latitude, double longitude) {

    public Coordinates {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new RoutingValidationException("Latitude hors bornes [-90, 90] : " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new RoutingValidationException("Longitude hors bornes [-180, 180] : " + longitude);
        }
    }
}
