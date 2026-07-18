package com.urgencesante.availability.internal.domain.model;

/** Statut de disponibilité d'un service dans un établissement. */
public enum AvailabilityStatus {
    AVAILABLE,
    LIMITED,
    SATURATED,
    CLOSED,
    UNKNOWN
}
