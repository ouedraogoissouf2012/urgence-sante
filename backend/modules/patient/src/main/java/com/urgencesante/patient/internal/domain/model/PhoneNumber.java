package com.urgencesante.patient.internal.domain.model;

import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import java.util.regex.Pattern;

/**
 * Numéro de téléphone d'un patient, sous forme normalisée E.164-like
 * ({@code +} suivi de 8 à 15 chiffres). Sert d'identifiant unique du compte.
 *
 * <p>La normalisation retire espaces et séparateurs ({@code espace . - ( )}),
 * de sorte que deux écritures du même numéro produisent la même valeur — donc
 * la même clé d'unicité en base et la même comparaison à la connexion.
 */
public record PhoneNumber(String value) {

    private static final Pattern SEPARATORS = Pattern.compile("[\\s.\\-()]");
    private static final Pattern NORMALIZED = Pattern.compile("\\+\\d{8,15}");

    public PhoneNumber {
        if (value == null || !NORMALIZED.matcher(value).matches()) {
            throw new PatientValidationException(
                    "Numéro de téléphone invalide (format attendu : indicatif international, ex. +225XXXXXXXXXX)");
        }
    }

    /** Construit à partir d'une saisie brute : nettoie puis valide. */
    public static PhoneNumber of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new PatientValidationException("Le numéro de téléphone est requis");
        }
        final String cleaned = SEPARATORS.matcher(raw.trim()).replaceAll("");
        return new PhoneNumber(cleaned);
    }
}
