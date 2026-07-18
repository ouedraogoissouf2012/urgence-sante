package com.urgencesante.availability;

import java.util.List;
import java.util.UUID;

/** Vue publique de la disponibilité de tous les services d'un établissement. */
public record FacilityAvailabilityView(UUID facilityId, List<ServiceAvailabilityView> services) {
}
