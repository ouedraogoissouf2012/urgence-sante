package com.urgencesante.medicalservice.internal.domain.model;

import com.urgencesante.medicalservice.internal.domain.exception.MedicalServiceValidationException;
import java.util.Locale;

/** Code unique et stable d'un service médical. Value object normalisé. */
public record MedicalServiceCode(String value) {

    public MedicalServiceCode {
        if (value == null || value.isBlank()) {
            throw new MedicalServiceValidationException("Le code de service médical est requis");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    public static MedicalServiceCode of(String raw) {
        return new MedicalServiceCode(raw);
    }
}
