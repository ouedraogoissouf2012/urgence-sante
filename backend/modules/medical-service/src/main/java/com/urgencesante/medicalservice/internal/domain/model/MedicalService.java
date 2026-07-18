package com.urgencesante.medicalservice.internal.domain.model;

import com.urgencesante.medicalservice.internal.domain.exception.MedicalServiceValidationException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Service médical du catalogue. Agrégat du domaine, sans dépendance framework.
 * L'identité repose sur le {@link MedicalServiceCode}.
 */
public final class MedicalService {

    private final MedicalServiceCode code;
    private final String label;
    private final String category;

    private MedicalService(MedicalServiceCode code, String label, String category) {
        this.code = Objects.requireNonNull(code, "Le code est requis");
        this.label = requireLabel(label);
        this.category = normalizeCategory(category);
    }

    public static MedicalService of(MedicalServiceCode code, String label, String category) {
        return new MedicalService(code, label, category);
    }

    private static String requireLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new MedicalServiceValidationException("Le libellé du service est requis");
        }
        return label.trim();
    }

    private static String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        final String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    public MedicalServiceCode code() {
        return code;
    }

    public String label() {
        return label;
    }

    public Optional<String> category() {
        return Optional.ofNullable(category);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof MedicalService service && code.equals(service.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
