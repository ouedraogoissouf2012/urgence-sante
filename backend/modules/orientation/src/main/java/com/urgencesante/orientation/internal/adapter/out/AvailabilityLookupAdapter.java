package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.availability.AvailabilityFacade;
import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort;
import java.util.List;
import java.util.Set;
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
    public List<ServiceStatus> statusesFor(UUID facilityId, Set<String> serviceCodes) {
        // UN seul accès à la disponibilité du centre, puis filtrage EN MÉMOIRE sur
        // les services demandés — au lieu d'un accès distinct (rechargeant toute
        // la liste) par service.
        return availabilityFacade.forFacility(facilityId).services().stream()
                .filter(service -> serviceCodes.stream()
                        .anyMatch(code -> code.equalsIgnoreCase(service.serviceCode())))
                .map(service -> new ServiceStatus(service.status(), service.freshness()))
                .toList();
    }
}
