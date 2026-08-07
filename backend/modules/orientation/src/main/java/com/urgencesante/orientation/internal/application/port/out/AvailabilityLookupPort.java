package com.urgencesante.orientation.internal.application.port.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Port sortant : statuts de disponibilité des services d'un établissement. */
public interface AvailabilityLookupPort {

    /**
     * Statuts des services demandés pour un établissement, en UN SEUL accès par
     * établissement. La disponibilité est ainsi chargée une fois par candidat, et
     * non une fois par (candidat × service) — le coût ne croît plus avec le
     * nombre de services demandés. Les services sans statut connu sont
     * simplement absents de la liste renvoyée.
     */
    List<ServiceStatus> statusesFor(UUID facilityId, Set<String> serviceCodes);

    /** Statut et fraîcheur bruts, tels qu'exposés par le module availability. */
    record ServiceStatus(String status, String freshness) {
    }
}
