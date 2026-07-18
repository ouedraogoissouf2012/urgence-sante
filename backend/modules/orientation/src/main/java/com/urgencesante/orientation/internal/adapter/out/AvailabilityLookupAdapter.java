package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.availability.AvailabilityFacade;
import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Consulte la disponibilité via l'API publique du module Availability. */
@Component
class AvailabilityLookupAdapter implements AvailabilityLookupPort {

    private final AvailabilityFacade availabilityFacade;

    AvailabilityLookupAdapter(AvailabilityFacade availabilityFacade) {
        this.availabilityFacade = availabilityFacade;
    }

    @Override
    public Optional<ServiceStatus> lookup(UUID facilityId, String serviceCode) {
        return availabilityFacade.forFacility(facilityId).services().stream()
                .filter(service -> service.serviceCode().equalsIgnoreCase(serviceCode))
                .findFirst()
                .map(service -> new ServiceStatus(service.status(), service.freshness()));
    }
}
