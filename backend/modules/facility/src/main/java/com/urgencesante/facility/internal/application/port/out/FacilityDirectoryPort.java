package com.urgencesante.facility.internal.application.port.out;

import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;

/** Port sortant : écriture idempotente de l'annuaire et garde des données démo. */
public interface FacilityDirectoryPort {

    /** Vrai si un établissement existe déjà pour la clé naturelle (source, réf). */
    boolean existsByNaturalKey(String source, String externalRef);

    /** Insère ou met à jour l'établissement par sa clé naturelle (idempotent). */
    void upsert(FacilityImportRecord record);

    /** Vrai si des données de démonstration (fictives) sont présentes. */
    boolean hasDemoData();

    /** Vrai si au moins un établissement non-démo (VERIFIED ou PROVISIONAL) existe. */
    boolean hasNonDemoData();

    /**
     * Purge les données de démonstration (fictives). Appelant : voir
     * {@code FacilityImportRunner}, qui n'appelle cette méthode QU'après avoir
     * confirmé {@link #hasNonDemoData()} — ne jamais purger sans donnée de
     * remplacement, sous peine de vider l'annuaire.
     */
    void purgeDemoData();
}
