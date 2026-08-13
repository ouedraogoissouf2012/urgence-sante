package com.urgencesante.buildingblocks.pagination;

/**
 * Demande de pagination : index de page (à partir de 0) et taille de page.
 * Value object partagé, sans métier.
 */
public record PageRequest(int page, int size) {

    public static final int MAX_SIZE = 100;

    /**
     * Plafond du numéro de page (audit P3 #140 : pagination profonde par
     * OFFSET). Sans borne haute, {@code page} n'importe quelle valeur (ex.
     * {@code Integer.MAX_VALUE}) produit un {@code OFFSET} arbitrairement
     * grand sur une requête triée — coûteux pour la base (tri/scan de tout ce
     * qui précède avant de le jeter) pour une réponse presque toujours vide en
     * pratique. Combiné à {@link #MAX_SIZE}, borne l'OFFSET maximal atteignable
     * à {@code MAX_PAGE * MAX_SIZE} (10 000 * 100 = 1 000 000) — trois ordres
     * de grandeur au-delà de tout annuaire réaliste pour ce produit, sans
     * empêcher un usage légitime.
     */
    public static final int MAX_PAGE = 10_000;

    public PageRequest {
        if (page < 0 || page > MAX_PAGE) {
            throw new IllegalArgumentException(
                    "Le numéro de page doit être dans [0, " + MAX_PAGE + "] : " + page);
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
