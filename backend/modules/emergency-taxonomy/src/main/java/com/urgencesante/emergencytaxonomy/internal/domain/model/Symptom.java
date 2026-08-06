package com.urgencesante.emergencytaxonomy.internal.domain.model;

/**
 * Symptôme sélectionnable au second niveau de la taxonomie. {@code id} est un
 * identifiant stable (stockage/analytics), {@code label} le libellé affiché.
 * Value object immuable ; l'identité repose sur {@code id}.
 */
public record Symptom(String id, String label) {

    public Symptom {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant du symptôme est requis");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Le libellé du symptôme est requis");
        }
        id = id.trim();
        label = label.trim();
    }

    public static Symptom of(String id, String label) {
        return new Symptom(id, label);
    }
}
