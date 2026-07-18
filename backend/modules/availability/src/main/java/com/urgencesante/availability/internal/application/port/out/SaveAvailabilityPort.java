package com.urgencesante.availability.internal.application.port.out;

import com.urgencesante.availability.internal.domain.model.Availability;

/**
 * Port sortant : persiste la disponibilité courante (remplacée) et en conserve
 * une trace dans l'historique auditable.
 */
public interface SaveAvailabilityPort {

    void save(Availability availability);
}
