package com.urgencesante.routing;

/** Vue publique d'un itinéraire (distance en mètres, durée en secondes). */
public record RouteView(double distanceMeters, double durationSeconds) {
}
