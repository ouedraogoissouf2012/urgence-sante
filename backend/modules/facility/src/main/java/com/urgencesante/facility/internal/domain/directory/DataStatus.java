package com.urgencesante.facility.internal.domain.directory;

/** Statut d'une donnée d'annuaire. */
public enum DataStatus {
    /** Vérifiée par un responsable, avec date de vérification. */
    VERIFIED,

    /** Provisoire : à confirmer par une revue manuelle. */
    PROVISIONAL,

    /** Fictive (démonstration) : INTERDITE en production. */
    DEMO
}
