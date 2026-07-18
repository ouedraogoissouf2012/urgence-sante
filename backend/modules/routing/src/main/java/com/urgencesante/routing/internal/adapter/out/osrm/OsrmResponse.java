package com.urgencesante.routing.internal.adapter.out.osrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Sous-ensemble de la réponse OSRM /route utilisé par l'adaptateur. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmResponse(String code, List<OsrmRoute> routes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OsrmRoute(double distance, double duration) {
    }
}
