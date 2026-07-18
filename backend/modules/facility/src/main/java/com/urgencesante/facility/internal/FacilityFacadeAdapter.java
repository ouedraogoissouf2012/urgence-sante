package com.urgencesante.facility.internal;

import com.urgencesante.buildingblocks.pagination.PageRequest;
import com.urgencesante.facility.FacilityFacade;
import com.urgencesante.facility.FacilityView;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.application.port.out.LoadFacilityPort;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module en s'appuyant sur le port de lecture. */
@Component
class FacilityFacadeAdapter implements FacilityFacade {

    private final LoadFacilityPort loadFacilityPort;

    FacilityFacadeAdapter(LoadFacilityPort loadFacilityPort) {
        this.loadFacilityPort = loadFacilityPort;
    }

    @Override
    public Optional<FacilityView> findById(UUID id) {
        return loadFacilityPort.findById(FacilityId.of(id)).map(FacilityFacadeAdapter::toView);
    }

    @Override
    public List<FacilityView> findNearbyOffering(
            String serviceCode, double latitude, double longitude, int radiusMeters, int limit) {
        final FindFacilitiesQuery query = new FindFacilitiesQuery(
                Optional.of(MedicalServiceCode.of(serviceCode)),
                Optional.of(new GeoLocation(latitude, longitude)),
                Optional.of(radiusMeters),
                PageRequest.of(0, limit));
        return loadFacilityPort.search(query).content().stream()
                .map(FacilityFacadeAdapter::toView)
                .toList();
    }

    private static FacilityView toView(Facility facility) {
        return new FacilityView(
                facility.id().value(),
                facility.name(),
                facility.location().latitude(),
                facility.location().longitude(),
                facility.services().stream()
                        .map(MedicalServiceCode::value)
                        .collect(Collectors.toUnmodifiableSet()));
    }
}
