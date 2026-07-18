package com.urgencesante.orientation.internal.domain.model;

/** Distance orthodromique (Haversine) entre deux points, en mètres. */
public final class GeoDistance {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoDistance() {
    }

    public static double meters(double lat1, double lon1, double lat2, double lon2) {
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
