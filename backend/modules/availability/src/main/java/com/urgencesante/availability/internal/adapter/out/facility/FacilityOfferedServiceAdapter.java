package com.urgencesante.availability.internal.adapter.out.facility;

import com.urgencesante.availability.internal.application.port.out.OfferedServicePort;
import com.urgencesante.facility.FacilityFacade;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Vérifie l'offre de service via l'API publique du module Facility
 * (dépendance autorisée par la matrice : availability → facility API).
 */
@Component
class FacilityOfferedServiceAdapter implements OfferedServicePort {

    private final FacilityFacade facilityFacade;

    FacilityOfferedServiceAdapter(FacilityFacade facilityFacade) {
        this.facilityFacade = facilityFacade;
    }

    @Override
    public boolean offers(UUID facilityId, String serviceCode) {
        final String normalized = serviceCode.trim().toLowerCase(Locale.ROOT);
        return facilityFacade.findById(facilityId)
                .map(facility -> facility.services().contains(normalized))
                .orElse(false);
    }
}
