package com.urgencesante.patient.internal.adapter.in.web;

import com.urgencesante.patient.internal.domain.exception.InvalidCredentialsException;
import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import com.urgencesante.patient.internal.domain.exception.PhoneAlreadyRegisteredException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduit les erreurs du domaine patient en réponses RFC 9457, limité à ce contrôleur. */
@RestControllerAdvice(assignableTypes = PatientController.class)
public class PatientExceptionHandler {

    @ExceptionHandler({PatientValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Requête invalide");
        return problem;
    }

    @ExceptionHandler(PhoneAlreadyRegisteredException.class)
    public ProblemDetail handleConflict(PhoneAlreadyRegisteredException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Numéro déjà utilisé");
        return problem;
    }

    /**
     * Perdant d'une course d'inscription (issue #131) : le contrôle applicatif
     * {@code existsByPhone} n'a rien vu, mais la contrainte d'unicité {@code
     * patient_account.phone} (V9) a rejeté l'insertion. Même réponse que le cas
     * non concurrent : le client ne doit pas voir de différence entre les deux.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrityViolation(DataIntegrityViolationException exception) {
        return handleConflict(new PhoneAlreadyRegisteredException());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleUnauthorized(InvalidCredentialsException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problem.setTitle("Authentification refusée");
        return problem;
    }
}
