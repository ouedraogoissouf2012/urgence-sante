package com.urgencesante.orientation.internal.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Port sortant : statut de disponibilité d'un service pour un établissement. */
public interface AvailabilityLookupPort {

    Optional<ServiceStatus> lookup(UUID facilityId, String serviceCode);

    /** Statut et fraîcheur bruts, tels qu'exposés par le module availability. */
    record ServiceStatus(String status, String freshness) {
    }
}
