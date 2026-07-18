package com.urgencesante.availability.internal.application.port.out;

import com.urgencesante.availability.internal.domain.model.Availability;
import java.util.List;
import java.util.UUID;

/** Port sortant : lecture de la disponibilité courante et de l'historique. */
public interface LoadAvailabilityPort {

    List<Availability> findByFacility(UUID facilityId);

    /** Mises à jour passées d'un service, la plus récente en premier. */
    List<Availability> history(UUID facilityId, String serviceCode, int limit);
}
