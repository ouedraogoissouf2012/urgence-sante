package com.urgencesante.availability.internal;

import com.urgencesante.availability.AvailabilityFacade;
import com.urgencesante.availability.FacilityAvailabilityView;
import com.urgencesante.availability.ServiceAvailabilityView;
import com.urgencesante.availability.internal.application.port.in.GetFacilityAvailabilityUseCase;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module à partir du cas d'usage de lecture. */
@Component
class AvailabilityFacadeAdapter implements AvailabilityFacade {

    private final GetFacilityAvailabilityUseCase getFacilityAvailability;

    AvailabilityFacadeAdapter(GetFacilityAvailabilityUseCase getFacilityAvailability) {
        this.getFacilityAvailability = getFacilityAvailability;
    }

    @Override
    public FacilityAvailabilityView forFacility(UUID facilityId) {
        final FacilityAvailabilitySnapshot snapshot = getFacilityAvailability.forFacility(facilityId);
        return new FacilityAvailabilityView(
                snapshot.facilityId(),
                snapshot.services().stream().map(AvailabilityFacadeAdapter::toView).toList());
    }

    private static ServiceAvailabilityView toView(ServiceAvailabilitySnapshot snapshot) {
        return new ServiceAvailabilityView(
                snapshot.serviceCode(),
                snapshot.status().name(),
                snapshot.freshness().name(),
                snapshot.updatedAt());
    }
}
