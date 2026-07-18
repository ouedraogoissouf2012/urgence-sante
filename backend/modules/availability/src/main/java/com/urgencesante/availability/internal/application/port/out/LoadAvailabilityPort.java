package com.urgencesante.availability.internal.application.port.out;

import com.urgencesante.availability.internal.domain.model.Availability;
import java.util.List;
import java.util.UUID;

/** Port sortant : lecture de la disponibilité courante d'un établissement. */
public interface LoadAvailabilityPort {

    List<Availability> findByFacility(UUID facilityId);
}
