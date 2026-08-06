package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.facility.FacilityFacade;
import com.urgencesante.orientation.internal.application.port.out.CandidateFacilityPort;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Fournit les candidats via l'API publique du module Facility. */
@Component
class FacilityCandidateAdapter implements CandidateFacilityPort {

    private final FacilityFacade facilityFacade;

    FacilityCandidateAdapter(FacilityFacade facilityFacade) {
        this.facilityFacade = facilityFacade;
    }

    @Override
    public List<CandidateFacility> findCandidates(
            Set<String> serviceCodes, double latitude, double longitude, int radiusMeters, int limit) {
        return facilityFacade.findNearbyOfferingAny(serviceCodes, latitude, longitude, radiusMeters, limit)
                .stream()
                .map(view -> new CandidateFacility(
                        view.id(), view.name(), view.latitude(), view.longitude(), view.phone()))
                .toList();
    }
}
