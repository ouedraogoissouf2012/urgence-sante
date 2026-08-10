package com.urgencesante.orientation.internal.adapter.out;

import com.urgencesante.availability.AvailabilityFacade;
import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Dette tracée (issue #127) : {@link AvailabilityFacade} n'expose à ce jour
     * que {@code forFacility(UUID)} (un établissement à la fois) — cette
     * frontière appartient au module {@code availability}, hors périmètre de ce
     * correctif (module en cours d'évolution en parallèle). Ce port GROUPE donc
     * déjà correctement l'appel côté orchestration (un seul appel quel que soit
     * le nombre de candidats, cf. {@code OrientationService}), mais TANT QUE la
     * façade n'expose pas de méthode batch, cet adaptateur continue d'émettre un
     * accès base par établissement en aval. Fermer complètement l'issue #127
     * nécessite d'ajouter une méthode groupée à {@link AvailabilityFacade}.
     */
    @Override
    public Map<UUID, List<ServiceStatus>> statusesForFacilities(
            Set<UUID> facilityIds, Set<String> serviceCodes) {
        final Map<UUID, List<ServiceStatus>> result = new LinkedHashMap<>();
        for (final UUID facilityId : facilityIds) {
            // UN accès à la disponibilité du centre, puis filtrage EN MÉMOIRE sur
            // les services demandés — au lieu d'un accès distinct (rechargeant
            // toute la liste) par service (issue #110).
            final List<ServiceStatus> statuses = availabilityFacade.forFacility(facilityId).services().stream()
                    .filter(service -> serviceCodes.stream()
                            .anyMatch(code -> code.equalsIgnoreCase(service.serviceCode())))
                    .map(service -> new ServiceStatus(service.status(), service.freshness()))
                    .toList();
            result.put(facilityId, statuses);
        }
        return result;
    }
}
