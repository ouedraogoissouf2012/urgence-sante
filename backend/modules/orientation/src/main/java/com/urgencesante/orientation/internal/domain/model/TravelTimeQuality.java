package com.urgencesante.orientation.internal.domain.model;

/** Qualification du temps de trajet présenté à l'utilisateur. */
public enum TravelTimeQuality {
    /** Temps réel calculé par le fournisseur d'itinéraires. */
    REAL,

    /** Temps estimé depuis la distance (fournisseur indisponible). */
    ESTIMATED,

    /** Aucun temps présentable. */
    UNAVAILABLE
}
