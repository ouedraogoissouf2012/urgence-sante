package com.urgencesante.availability.internal.application.result;

import java.util.List;
import java.util.UUID;

/** Disponibilité courante de tous les services d'un établissement. */
public record FacilityAvailabilitySnapshot(UUID facilityId, List<ServiceAvailabilitySnapshot> services) {
}
