package com.urgencesante.facility.internal.domain.directory;

/**
 * Emprise géographique du Grand Abidjan (boîte englobante WGS84).
 *
 * <p>Sert à rejeter à l'import toute coordonnée hors zone. Bornes volontairement
 * généreuses pour couvrir les communes périphériques (Bingerville, Songon,
 * Anyama) sans laisser passer des points manifestement hors district.
 */
public final class AbidjanZone {

    public static final double MIN_LATITUDE = 5.15;
    public static final double MAX_LATITUDE = 5.55;
    public static final double MIN_LONGITUDE = -4.35;
    public static final double MAX_LONGITUDE = -3.75;

    private AbidjanZone() {
    }

    public static boolean contains(double latitude, double longitude) {
        return latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE
                && longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }
}
