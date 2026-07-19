package com.urgencesante.facility.internal.domain.directory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Valide un enregistrement d'import (pur). Retourne la liste des motifs de rejet
 * (vide si l'enregistrement est acceptable). Contrôle : champs requis,
 * traçabilité, coordonnées DANS le Grand Abidjan, téléphone ivoirien, et codes
 * de service connus (via un prédicat fourni).
 */
public final class FacilityImportValidator {

    /**
     * Téléphone ivoirien : +225 optionnel puis 10 chiffres commençant par 0
     * (numérotation post-2021 : 01/05/07 mobile, 21/25/27 fixe). Rejette les
     * suites triviales comme « 0000000000 » (préfixe opérateur invalide).
     */
    private static final Pattern CI_PHONE = Pattern.compile("(\\+225)?0[157]\\d{8}|(\\+225)?2[157]\\d{8}");

    private final Predicate<String> knownService;

    public FacilityImportValidator(Predicate<String> knownService) {
        this.knownService = knownService;
    }

    public List<String> validate(FacilityImportRecord record) {
        final List<String> reasons = new ArrayList<>();

        if (isBlank(record.source()) || isBlank(record.externalRef())) {
            reasons.add("provenance (source, external_ref) requise");
        }
        if (isBlank(record.name())) {
            reasons.add("nom requis");
        }
        if (record.dataStatus() == null) {
            reasons.add("statut de donnée requis");
        } else if (record.dataStatus() == DataStatus.VERIFIED && record.verifiedAt() == null) {
            reasons.add("date de vérification requise pour une donnée VERIFIED");
        }
        if (!AbidjanZone.contains(record.latitude(), record.longitude())) {
            reasons.add("coordonnées hors du Grand Abidjan");
        }
        if (record.phone() != null && !CI_PHONE.matcher(normalizePhone(record.phone())).matches()) {
            reasons.add("téléphone invalide : " + record.phone());
        }
        if (record.services().isEmpty()) {
            reasons.add("au moins un service requis");
        }
        for (final String service : record.services()) {
            if (!knownService.test(service)) {
                reasons.add("service inconnu : " + service);
            }
        }
        return reasons;
    }

    /** Normalise un téléphone pour la validation (retire espaces et séparateurs). */
    public static String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[\\s.\\-()]", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
