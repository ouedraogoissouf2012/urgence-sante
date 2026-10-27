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
 *
 * <p>Un numéro ivoirien ({@code +225}) saisi pour CRÉER un compte
 * ({@link #forNewAccount(String)}) doit en plus respecter le plan de
 * numérotation de 2021 : 10 chiffres commençant par 01, 05, 07 (mobiles), 21,
 * 25 ou 27 (fixes) — même règle que l'application, mais le serveur reste juge
 * (ancienne version de l'app, appel direct de l'API).
 *
 * <p>La CONNEXION ({@link #of(String)}) et la relecture depuis la base
 * (constructeur) n'appliquent que la règle générale : un compte créé avant ce
 * durcissement reste chargeable ET joignable ; et un numéro hors plan, inconnu,
 * suit le chemin à temps constant de la connexion (comparaison BCrypt), sans
 * réponse plus rapide qui distinguerait les formats.
 */
public record PhoneNumber(String value) {

    private static final Pattern SEPARATORS = Pattern.compile("[\\s.\\-()]");
    private static final Pattern NORMALIZED = Pattern.compile("\\+\\d{8,15}");
    private static final String IVORY_COAST_DIAL_CODE = "+225";
    private static final Pattern IVORY_COAST = Pattern.compile("\\+225(01|05|07|21|25|27)\\d{8}");

    public PhoneNumber {
        if (value == null || !NORMALIZED.matcher(value).matches()) {
            throw new PatientValidationException(
                    "Numéro de téléphone invalide (format attendu : indicatif international, ex. +225XXXXXXXXXX)");
        }
    }

    /** Construit à partir d'une saisie brute (connexion) : nettoie puis valide. */
    public static PhoneNumber of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new PatientValidationException("Le numéro de téléphone est requis");
        }
        final String cleaned = SEPARATORS.matcher(raw.trim()).replaceAll("");
        return new PhoneNumber(cleaned);
    }

    /** Numéro saisi pour créer un compte : règle générale + plan ivoirien. */
    public static PhoneNumber forNewAccount(String raw) {
        final PhoneNumber phone = of(raw);
        if (phone.value.startsWith(IVORY_COAST_DIAL_CODE) && !IVORY_COAST.matcher(phone.value).matches()) {
            throw new PatientValidationException(
                    "Numéro ivoirien invalide : 10 chiffres commençant par 01, 05, 07, 21, 25 ou 27 "
                            + "(ex. +225 05 44 21 01 12)");
        }
        return phone;
    }
}
