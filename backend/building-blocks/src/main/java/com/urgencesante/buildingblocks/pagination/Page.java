package com.urgencesante.buildingblocks.pagination;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Page de résultats immuable. Value object partagé, sans métier.
 *
 * @param <T> type des éléments de la page
 */
public record Page<T>(List<T> content, int number, int size, long totalElements) {

    public Page {
        content = List.copyOf(Objects.requireNonNull(content, "Le contenu est requis"));
        if (number < 0) {
            throw new IllegalArgumentException("Le numéro de page doit être >= 0 : " + number);
        }
        if (size < 1) {
            throw new IllegalArgumentException("La taille de page doit être >= 1 : " + size);
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Le total doit être >= 0 : " + totalElements);
        }
    }

    /** Nombre total de pages pour ce total d'éléments. */
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    /** Transforme chaque élément en conservant les métadonnées de pagination. */
    public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Le mapper est requis");
        return new Page<>(content.stream().<R>map(mapper).toList(), number, size, totalElements);
    }
}
