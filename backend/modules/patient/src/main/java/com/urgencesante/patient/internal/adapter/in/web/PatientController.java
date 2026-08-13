package com.urgencesante.patient.internal.adapter.in.web;

import com.urgencesante.patient.internal.adapter.in.web.dto.request.LoginRequest;
import com.urgencesante.patient.internal.adapter.in.web.dto.request.RegisterPatientRequest;
import com.urgencesante.patient.internal.adapter.in.web.dto.response.PatientSessionResponse;
import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.port.in.AuthenticatePatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RegisterPatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RevokePatientSessionUseCase;
import com.urgencesante.patient.internal.application.result.PatientSession;
import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final RevokePatientSessionUseCase revokeSession;

    public PatientController(
            RegisterPatientUseCase registerPatient,
            AuthenticatePatientUseCase authenticatePatient,
            RevokePatientSessionUseCase revokeSession) {
        this.registerPatient = registerPatient;
        this.authenticatePatient = authenticatePatient;
        this.revokeSession = revokeSession;
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

    /**
     * Révocation (déconnexion) : invalide le jeton présenté avant son
     * expiration naturelle (audit P3 #140). Idempotent — un jeton déjà
     * révoqué, expiré ou inconnu renvoie aussi 204.
     */
    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        // required = false : un en-tête absent doit passer par bearerToken()
        // ci-dessous (400 "Requête invalide", cohérent avec le reste du
        // contrôleur) plutôt que par MissingRequestHeaderException, que ni
        // PatientExceptionHandler ni le filet de dernier recours (#140 pt. 4)
        // ne mappent explicitement en 400.
        revokeSession.revoke(bearerToken(authorization));
    }

    /** Extraction du jeton porteur (même convention que le portail hospitalier). */
    private static String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new PatientValidationException("En-tête Authorization: Bearer <jeton> requis");
        }
        final String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new PatientValidationException("En-tête Authorization: Bearer <jeton> requis");
        }
        return token;
    }
}
