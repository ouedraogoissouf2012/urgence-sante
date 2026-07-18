package com.urgencesante.availability.internal.application.port.in;

import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import java.util.UUID;

/** Port entrant : consulter la disponibilité courante d'un établissement. */
public interface GetFacilityAvailabilityUseCase {

    FacilityAvailabilitySnapshot forFacility(UUID facilityId);
}
