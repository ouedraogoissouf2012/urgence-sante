package com.urgencesante.patient.internal.adapter.in.web;

import com.urgencesante.patient.internal.adapter.in.web.dto.request.LoginRequest;
import com.urgencesante.patient.internal.adapter.in.web.dto.request.RegisterPatientRequest;
import com.urgencesante.patient.internal.adapter.in.web.dto.response.PatientSessionResponse;
import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.port.in.AuthenticatePatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RegisterPatientUseCase;
import com.urgencesante.patient.internal.application.result.PatientSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST des comptes patients. Conforme au contrat OpenAPI.
 * Aucune logique métier : validation déléguée au domaine, appel des use cases,
 * mapping DTO. L'inscription et la connexion sont publiques (rate-limitées par
 * l'assemblage) ; le jeton renvoyé sert ensuite aux futurs endpoints protégés.
 */
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final RegisterPatientUseCase registerPatient;
    private final AuthenticatePatientUseCase authenticatePatient;

    public PatientController(
            RegisterPatientUseCase registerPatient,
            AuthenticatePatientUseCase authenticatePatient) {
        this.registerPatient = registerPatient;
        this.authenticatePatient = authenticatePatient;
    }

    /**
     * Inscription : crée le compte et ouvre immédiatement une session, dans la
     * même transaction applicative (issue #130) — jamais de compte sans
     * session initiale.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientSessionResponse register(@RequestBody RegisterPatientRequest request) {
        final PatientSession session = registerPatient.register(
                new RegisterPatientCommand(request.phone(), request.password()));
        return new PatientSessionResponse(session.patientId(), session.token());
    }

    /** Connexion : vérifie les identifiants et renvoie un jeton de session. */
    @PostMapping("/session")
    public PatientSessionResponse login(@RequestBody LoginRequest request) {
        final PatientSession session = authenticatePatient.login(
                new LoginCommand(request.phone(), request.password()));
        return new PatientSessionResponse(session.patientId(), session.token());
    }
}
