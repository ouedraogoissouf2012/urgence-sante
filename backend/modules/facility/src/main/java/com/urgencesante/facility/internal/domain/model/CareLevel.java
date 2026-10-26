package com.urgencesante.facility.internal.domain.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Niveau de soins d'un établissement dans la pyramide sanitaire (vocabulaire du
 * client : « centre de premier / deuxième / troisième niveau »).
 *
 * <p>Porte la règle d'orientation demandée par le client (ADR-008) : un centre
 * de premier niveau ne traite pas les urgences et n'est donc JAMAIS proposé ;
 * les deuxième et troisième niveaux le sont. Distinguer « certaines » urgences
 * (2e niveau) de « toutes » (3e niveau) reste le rôle des services offerts : un
 * centre n'est proposé que pour un besoin couvert par ses services.
 */
public enum CareLevel {

    /** Premier niveau (centres de santé de proximité) : ne traite pas les urgences. */
    PRIMARY(false),

    /** Deuxième niveau (hôpitaux généraux) : traite certaines urgences. */
    SECONDARY(true),

    /** Troisième niveau (CHU, établissements de référence) : traite toutes les urgences. */
    TERTIARY(true);

    private static final Set<CareLevel> EMERGENCY_CAPABLE =
            Set.copyOf(Arrays.stream(values()).filter(CareLevel::isEmergencyCapable).toList());

    private final boolean emergencyCapable;

    CareLevel(boolean emergencyCapable) {
        this.emergencyCapable = emergencyCapable;
    }

    /** Vrai si un établissement de ce niveau peut être proposé pour une urgence. */
    public boolean isEmergencyCapable() {
        return emergencyCapable;
    }

    /** Niveaux dont les établissements peuvent être proposés pour une urgence (non modifiable). */
    public static Set<CareLevel> emergencyCapable() {
        return EMERGENCY_CAPABLE;
    }

    /**
     * Interprète un code de niveau ({@code PRIMARY}, {@code SECONDARY},
     * {@code TERTIARY}), insensible à la casse et aux espaces. Vide si le code
     * est absent ou inconnu : à l'appelant d'en faire un motif de rejet.
     */
    public static Optional<CareLevel> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        final String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(level -> level.name().equals(normalized))
                .findFirst();
    }
}
