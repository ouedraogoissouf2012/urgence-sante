package com.urgencesante.buildingblocks.medicalservice;

import java.util.Locale;

/**
 * Code d'un service médical (ex. « maternity »). Value object immuable et
 * normalisé, partagé par les modules qui référencent le catalogue des
 * services (facility, medical-service) — la validation ne doit exister qu'à
 * un seul endroit.
 */
public record MedicalServiceCode(String value) {

    /** Longueur maximale d'un code (alignée sur le contrat OpenAPI). */
    public static final int MAX_LENGTH = 64;

    public MedicalServiceCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Le code de service médical est requis");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Code de service trop long (max " + MAX_LENGTH + " caractères)");
        }
    }

    public static MedicalServiceCode of(String raw) {
        return new MedicalServiceCode(raw);
    }
}
