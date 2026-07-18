package com.urgencesante.medicalservice.internal.adapter.in.web;

import com.urgencesante.medicalservice.internal.domain.exception.MedicalServiceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les erreurs du domaine en réponses RFC 9457. Portée limitée au
 * contrôleur du module.
 */
@RestControllerAdvice(assignableTypes = MedicalServiceController.class)
public class MedicalServiceExceptionHandler {

    @ExceptionHandler({MedicalServiceValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Requête invalide");
        return problem;
    }
}
