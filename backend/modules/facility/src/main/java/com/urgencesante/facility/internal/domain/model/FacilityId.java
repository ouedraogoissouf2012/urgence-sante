package com.urgencesante.facility.internal.domain.model;

import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import java.util.Objects;
import java.util.UUID;

/**
 * Identifiant unique d'un établissement. Value object immuable.
 */
public record FacilityId(UUID value) {

    public FacilityId {
        Objects.requireNonNull(value, "L'identifiant d'établissement est requis");
    }

    public static FacilityId of(UUID value) {
        return new FacilityId(value);
    }

    /** Parse une représentation textuelle, en erreur métier si invalide. */
    public static FacilityId fromString(String raw) {
        try {
            return new FacilityId(UUID.fromString(raw));
        } catch (IllegalArgumentException e) {
            throw new FacilityValidationException("Identifiant d'établissement invalide : " + raw);
        }
    }
}
