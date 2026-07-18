package com.urgencesante.medicalservice.internal.application.port.in;

import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import java.util.List;
import java.util.Optional;

/** Port entrant : lister le catalogue des services médicaux. */
public interface ListMedicalServicesUseCase {

    List<MedicalService> list(Optional<String> category);
}
