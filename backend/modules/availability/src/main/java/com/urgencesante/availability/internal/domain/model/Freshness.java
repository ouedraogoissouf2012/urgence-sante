package com.urgencesante.availability.internal.domain.model;

/** Fraîcheur d'une information de disponibilité, dérivée de son ancienneté. */
public enum Freshness {
    FRESH,
    AGING,
    STALE
}
