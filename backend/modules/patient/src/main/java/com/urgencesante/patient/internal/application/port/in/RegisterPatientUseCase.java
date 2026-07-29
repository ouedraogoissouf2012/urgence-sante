package com.urgencesante.patient.internal.application.port.in;

import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import java.util.UUID;

/** Inscription d'un nouveau patient (port entrant). */
public interface RegisterPatientUseCase {

    /** Crée le compte et renvoie son identifiant. */
    UUID register(RegisterPatientCommand command);
}
