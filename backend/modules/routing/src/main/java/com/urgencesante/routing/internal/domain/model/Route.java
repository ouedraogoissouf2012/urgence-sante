package com.urgencesante.routing.internal.domain.model;

import com.urgencesante.routing.internal.domain.exception.RoutingValidationException;

/** Itinéraire calculé : distance (mètres) et durée estimée (secondes). */
public record Route(double distanceMeters, double durationSeconds) {

    public Route {
        if (distanceMeters < 0) {
            throw new RoutingValidationException("La distance ne peut pas être négative : " + distanceMeters);
        }
        if (durationSeconds < 0) {
            throw new RoutingValidationException("La durée ne peut pas être négative : " + durationSeconds);
        }
    }
}
