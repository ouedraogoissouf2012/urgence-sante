package com.urgencesante.orientation.internal.application.port.out;

import java.util.OptionalDouble;

/** Port sortant : temps de trajet estimé entre deux points, en secondes. */
public interface TravelTimePort {

    OptionalDouble travelTimeSeconds(double fromLat, double fromLon, double toLat, double toLon);
}
