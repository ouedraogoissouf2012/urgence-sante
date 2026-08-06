package com.urgencesante.emergencytaxonomy.internal.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Grande catégorie d'urgence (premier niveau de sélection). Porte ses symptômes
 * (second niveau) et les codes des services médicaux « recherchés ».
 *
 * <p>Certaines catégories relèvent d'un <b>appel direct des secours</b>
 * ({@code directCallOnly}) : l'application affiche alors un message au lieu de
 * lancer une recherche de centres. Une telle catégorie doit porter ce message.
 *
 * <p>Agrégat immuable ; l'identité repose sur {@code id}. La validation échoue
 * vite (au démarrage) car la taxonomie est un référentiel rédigé par le dev.
 */
public final class EmergencyCategory {

    private final String id;
    private final String label;
    private final int order;
    private final boolean directCallOnly;
    private final String directCallMessage;
    private final List<Symptom> symptoms;
    private final List<String> serviceCodes;

    private EmergencyCategory(String id, String label, int order, boolean directCallOnly,
            String directCallMessage, List<Symptom> symptoms, List<String> serviceCodes) {
        this.id = requireText(id, "L'identifiant de la catégorie est requis").trim();
        this.label = requireText(label, "Le libellé de la catégorie est requis").trim();
        this.order = order;
        this.directCallOnly = directCallOnly;
        this.symptoms = List.copyOf(requireNotEmpty(symptoms, "symptômes"));
        this.serviceCodes = List.copyOf(requireNotEmpty(serviceCodes, "services recherchés"));
        // Invariant métier : une catégorie « appel direct » doit porter un message.
        if (directCallOnly && (directCallMessage == null || directCallMessage.isBlank())) {
            throw new IllegalArgumentException(
                    "Une catégorie à appel direct doit porter un message (catégorie " + id + ")");
        }
        this.directCallMessage =
                (directCallMessage == null || directCallMessage.isBlank()) ? null : directCallMessage.trim();
    }

    public static EmergencyCategory of(String id, String label, int order, boolean directCallOnly,
            String directCallMessage, List<Symptom> symptoms, List<String> serviceCodes) {
        return new EmergencyCategory(
                id, label, order, directCallOnly, directCallMessage, symptoms, serviceCodes);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static <T> List<T> requireNotEmpty(List<T> values, String what) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Au moins un élément est requis : " + what);
        }
        return values;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public int order() {
        return order;
    }

    public boolean directCallOnly() {
        return directCallOnly;
    }

    public Optional<String> directCallMessage() {
        return Optional.ofNullable(directCallMessage);
    }

    public List<Symptom> symptoms() {
        return symptoms;
    }

    public List<String> serviceCodes() {
        return serviceCodes;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof EmergencyCategory category && id.equals(category.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
