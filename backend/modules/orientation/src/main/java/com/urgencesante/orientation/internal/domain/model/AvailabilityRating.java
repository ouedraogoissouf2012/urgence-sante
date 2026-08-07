package com.urgencesante.orientation.internal.domain.model;

/**
 * Rating de disponibilité d'un service : source UNIQUE de l'ordre de préférence,
 * du score de classement, de l'éligibilité et de la raison affichée.
 *
 * <p>Partagé par la stratégie de disponibilité (qui en tire score/éligibilité/
 * raison) et par le moteur d'orientation (qui choisit le meilleur statut parmi
 * plusieurs services demandés). L'ordinal, du MOINS au PLUS favorable, fait
 * office de rang — évitant de dupliquer l'ordre des statuts à deux endroits.
 */
public enum AvailabilityRating {
    CLOSED(0.0, false, "service fermé"),
    SATURATED(20.0, true, "service saturé"),
    UNKNOWN(40.0, true, "disponibilité non confirmée"),
    LIMITED(60.0, true, "disponibilité limitée"),
    AVAILABLE(100.0, true, "service disponible");

    private static final String STALE = "STALE";

    private final double score;
    private final boolean eligible;
    private final String reason;

    AvailabilityRating(double score, boolean eligible, String reason) {
        this.score = score;
        this.eligible = eligible;
        this.reason = reason;
    }

    public double score() {
        return score;
    }

    public boolean eligible() {
        return eligible;
    }

    public String reason() {
        return reason;
    }

    /** Rating d'un statut DÉJÀ interprété (fraîcheur résolue en amont). */
    public static AvailabilityRating of(String status) {
        return switch (status) {
            case "AVAILABLE" -> AVAILABLE;
            case "LIMITED" -> LIMITED;
            case "SATURATED" -> SATURATED;
            case "CLOSED" -> CLOSED;
            default -> UNKNOWN;
        };
    }

    /** Rating brut : un statut périmé (STALE) est ramené à « non confirmé ». */
    public static AvailabilityRating fromRaw(String status, String freshness) {
        return STALE.equals(freshness) ? UNKNOWN : of(status);
    }
}
