package com.urgencesante.facility.internal.application.port.in;

import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;

/** Port entrant : consulter un établissement par identifiant. */
public interface GetFacilityUseCase {

    /**
     * @throws com.urgencesante.facility.internal.domain.exception.FacilityNotFoundException
     *     si aucun établissement ne correspond
     */
    Facility getFacility(FacilityId id);
}
