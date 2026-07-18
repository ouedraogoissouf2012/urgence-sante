package com.urgencesante.facility.internal.application.port.in;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.domain.model.Facility;

/** Port entrant : rechercher des établissements. */
public interface FindFacilitiesUseCase {

    Page<Facility> findFacilities(FindFacilitiesQuery query);
}
