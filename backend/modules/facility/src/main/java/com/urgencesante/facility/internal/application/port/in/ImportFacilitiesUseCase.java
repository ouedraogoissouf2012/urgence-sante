package com.urgencesante.facility.internal.application.port.in;

import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.util.List;

/** Cas d'usage entrant : importer un lot d'établissements dans l'annuaire. */
public interface ImportFacilitiesUseCase {

    ImportReport importDirectory(List<FacilityImportRecord> records);

    /**
     * Purge les données de démonstration (fictives) — UNIQUEMENT si l'annuaire
     * contient déjà au moins un établissement non-démo. Ne JAMAIS vider
     * l'annuaire : un import qui échoue entièrement (fichier vide, tout
     * rejeté) doit laisser les données de démonstration en place plutôt que de
     * ne rien servir du tout (issue #123).
     *
     * @return vrai si une purge a eu lieu, faux si elle a été refusée (aucun
     *     établissement non-démo en annuaire)
     */
    boolean purgeDemoDataIfReplaced();
}
