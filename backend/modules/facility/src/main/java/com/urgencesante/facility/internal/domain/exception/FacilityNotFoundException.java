package com.urgencesante.facility.internal.domain.exception;

import com.urgencesante.facility.internal.domain.model.FacilityId;

/** Aucun établissement ne correspond à l'identifiant demandé. */
public class FacilityNotFoundException extends RuntimeException {

    public FacilityNotFoundException(FacilityId id) {
        super("Établissement introuvable : " + id.value());
    }
}
