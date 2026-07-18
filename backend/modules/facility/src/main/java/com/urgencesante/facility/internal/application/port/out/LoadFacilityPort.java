package com.urgencesante.facility.internal.application.port.out;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import java.util.Optional;

/**
 * Port sortant : besoin de lecture des établissements exprimé par le métier.
 * Implémenté par un adaptateur de persistance (PostGIS).
 */
public interface LoadFacilityPort {

    /** Recherche paginée, filtrée et éventuellement triée par proximité. */
    Page<Facility> search(FindFacilitiesQuery query);

    /** Charge un établissement par son identifiant. */
    Optional<Facility> findById(FacilityId id);
}
