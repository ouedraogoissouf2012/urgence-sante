package com.urgencesante.medicalservice.internal.domain.model;

import com.urgencesante.medicalservice.internal.domain.exception.MedicalServiceValidationException;
import java.util.Locale;

/** Code unique et stable d'un service médical. Value object normalisé. */
public record MedicalServiceCode(String value) {

    /** Longueur maximale d'un code (alignée sur le contrat OpenAPI). */
    public static final int MAX_LENGTH = 64;

    public MedicalServiceCode {
        if (value == null || value.isBlank()) {
            throw new MedicalServiceValidationException("Le code de service médical est requis");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) {
            throw new MedicalServiceValidationException(
                    "Code de service trop long (max " + MAX_LENGTH + " caractères)");
        }
    }

    public static MedicalServiceCode of(String raw) {
        return new MedicalServiceCode(raw);
    }
}
