package com.urgencesante.routing.internal.domain.exception;

/** Aucun itinéraire n'a pu être calculé entre les deux points. */
public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException() {
        super("Aucun itinéraire disponible entre les points fournis");
    }
}
