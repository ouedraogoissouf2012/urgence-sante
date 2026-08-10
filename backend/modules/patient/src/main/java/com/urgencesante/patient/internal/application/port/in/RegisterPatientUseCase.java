package com.urgencesante.patient.internal.application.port.in;

import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.result.PatientSession;

/** Inscription d'un nouveau patient (port entrant). */
public interface RegisterPatientUseCase {

    /**
     * Crée le compte et ouvre sa première session dans LA MÊME transaction
     * (issue #130) : jamais de compte sans session initiale, jamais de session
     * orpheline d'un compte non committé.
     */
    PatientSession register(RegisterPatientCommand command);
}
