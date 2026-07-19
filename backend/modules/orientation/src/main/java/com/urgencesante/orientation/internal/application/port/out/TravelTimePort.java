package com.urgencesante.orientation.internal.application.port.out;

import java.util.List;
import java.util.OptionalDouble;

/**
 * Port sortant : temps de trajet estimés d'une origine vers plusieurs
 * destinations, en secondes, obtenus en UN SEUL appel fournisseur (la latence
 * ne dépend pas du nombre de candidats). Liste alignée sur les destinations
 * (élément vide si indisponible).
 */
public interface TravelTimePort {

    List<OptionalDouble> travelTimesSeconds(
            double fromLat, double fromLon, List<double[]> destinations);
}
