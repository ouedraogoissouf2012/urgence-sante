package com.urgencesante.buildingblocks.pagination;

/**
 * Demande de pagination : index de page (à partir de 0) et taille de page.
 * Value object partagé, sans métier.
 */
public record PageRequest(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Le numéro de page doit être >= 0 : " + page);
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "La taille de page doit être dans [1, " + MAX_SIZE + "] : " + size);
        }
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    /** Décalage (nombre d'éléments à sauter) pour cette page. */
    public long offset() {
        return (long) page * size;
    }
}
