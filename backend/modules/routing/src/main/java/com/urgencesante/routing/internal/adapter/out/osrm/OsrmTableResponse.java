package com.urgencesante.routing.internal.adapter.out.osrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Sous-ensemble de la réponse OSRM /table utilisé par l'adaptateur
 * (durées et distances de la source vers chaque destination).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmTableResponse(
        String code,
        List<List<Double>> durations,
        List<List<Double>> distances) {
}
