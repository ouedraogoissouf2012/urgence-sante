package com.urgencesante.orientation.internal.application.query;

import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import java.util.Objects;

/** Critères d'orientation : position du patient, besoin, rayon et nombre max. */
public record OrientationQuery(
        double latitude, double longitude, String serviceCode, int radiusMeters, int limit) {

    public OrientationQuery {
        Objects.requireNonNull(serviceCode, "Le service est requis");
        if (latitude < -90.0 || latitude > 90.0) {
            throw new OrientationValidationException("Latitude hors bornes [-90, 90] : " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new OrientationValidationException("Longitude hors bornes [-180, 180] : " + longitude);
        }
        if (radiusMeters < 1) {
            throw new OrientationValidationException("Le rayon doit être >= 1 mètre");
        }
        if (limit < 1) {
            throw new OrientationValidationException("La limite doit être >= 1");
        }
    }
}
