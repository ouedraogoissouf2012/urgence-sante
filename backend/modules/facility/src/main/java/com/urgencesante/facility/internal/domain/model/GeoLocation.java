package com.urgencesante.facility.internal.domain.model;

import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;

/**
 * Coordonnées géographiques (WGS84). Value object immuable et auto-validé.
 */
public record GeoLocation(double latitude, double longitude) {

    public GeoLocation {
        // NaN/Infini échappent aux comparaisons de bornes : rejet explicite.
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new FacilityValidationException(
                    "Coordonnées non finies (NaN/Infini) refusées");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new FacilityValidationException(
                    "Latitude hors bornes [-90, 90] : " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new FacilityValidationException(
                    "Longitude hors bornes [-180, 180] : " + longitude);
        }
    }

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /**
     * Distance orthodromique approximative (Haversine) vers un autre point, en
     * mètres. Sert à l'affichage ; le tri par proximité s'appuie sur PostGIS.
     */
    public double distanceMetersTo(GeoLocation other) {
        final double dLat = Math.toRadians(other.latitude - latitude);
        final double dLon = Math.toRadians(other.longitude - longitude);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
