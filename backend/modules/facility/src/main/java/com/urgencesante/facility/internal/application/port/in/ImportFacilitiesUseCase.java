package com.urgencesante.facility.internal.application.port.in;

import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.util.List;

/** Cas d'usage entrant : importer un lot d'établissements dans l'annuaire. */
public interface ImportFacilitiesUseCase {

    ImportReport importDirectory(List<FacilityImportRecord> records);
}
