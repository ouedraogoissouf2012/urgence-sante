package com.urgencesante.availability;

import java.util.UUID;

/**
 * API publique du module Availability. Permet aux autres modules (ex.
 * orientation) de consulter la disponibilité courante d'un établissement.
 */
public interface AvailabilityFacade {

    FacilityAvailabilityView forFacility(UUID facilityId);
}
