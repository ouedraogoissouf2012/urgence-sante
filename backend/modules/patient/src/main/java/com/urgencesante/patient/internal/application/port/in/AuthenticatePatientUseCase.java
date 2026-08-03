package com.urgencesante.patient.internal.application.port.in;

import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.result.PatientSession;

/** Authentification d'un patient (port entrant). */
public interface AuthenticatePatientUseCase {

    /**
     * Vérifie les identifiants et ouvre une session.
     *
     * @throws com.urgencesante.patient.internal.domain.exception.InvalidCredentialsException
     *     si le téléphone est inconnu ou le mot de passe incorrect (message
     *     identique dans les deux cas : ne pas révéler l'existence d'un compte).
     */
    PatientSession login(LoginCommand command);
}
