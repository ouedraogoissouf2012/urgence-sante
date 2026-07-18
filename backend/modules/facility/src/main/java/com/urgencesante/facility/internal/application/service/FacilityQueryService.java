package com.urgencesante.facility.internal.application.service;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.facility.internal.application.port.in.FindFacilitiesUseCase;
import com.urgencesante.facility.internal.application.port.in.GetFacilityUseCase;
import com.urgencesante.facility.internal.application.port.out.LoadFacilityPort;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.domain.exception.FacilityNotFoundException;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import java.util.Objects;

/**
 * Cas d'usage de lecture des établissements. Java pur : la dépendance sur le
 * port sortant est injectée par constructeur, l'assemblage Spring vit dans la
 * configuration du module.
 */
public class FacilityQueryService implements FindFacilitiesUseCase, GetFacilityUseCase {

    private final LoadFacilityPort loadFacilityPort;

    public FacilityQueryService(LoadFacilityPort loadFacilityPort) {
        this.loadFacilityPort = Objects.requireNonNull(loadFacilityPort, "Le port de chargement est requis");
    }

    @Override
    public Page<Facility> findFacilities(FindFacilitiesQuery query) {
        Objects.requireNonNull(query, "La requête est requise");
        return loadFacilityPort.search(query);
    }

    @Override
    public Facility getFacility(FacilityId id) {
        Objects.requireNonNull(id, "L'identifiant est requis");
        return loadFacilityPort.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }
}
