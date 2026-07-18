package com.urgencesante.orientation.internal.application.port.out;

import java.util.List;
import java.util.UUID;

/** Port sortant : établissements candidats offrant un service, par proximité. */
public interface CandidateFacilityPort {

    List<CandidateFacility> findCandidates(
            String serviceCode, double latitude, double longitude, int radiusMeters, int limit);

    /** Établissement candidat (données minimales pour l'évaluation). */
    record CandidateFacility(UUID facilityId, String name, double latitude, double longitude) {
    }
}
