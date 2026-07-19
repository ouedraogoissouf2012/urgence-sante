package com.urgencesante.facility.internal.domain.model;

import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import java.util.Locale;

/**
 * Code d'un service médical offert par un établissement (ex. « maternity »).
 * Value object immuable, normalisé en minuscules.
 */
public record MedicalServiceCode(String value) {

    /** Longueur maximale d'un code (alignée sur le contrat OpenAPI). */
    public static final int MAX_LENGTH = 64;

    public MedicalServiceCode {
        if (value == null || value.isBlank()) {
            throw new FacilityValidationException("Le code de service médical est requis");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) {
            throw new FacilityValidationException(
                    "Code de service trop long (max " + MAX_LENGTH + " caractères)");
        }
    }

    public static MedicalServiceCode of(String raw) {
        return new MedicalServiceCode(raw);
    }
}
