package com.urgencesante.facility.internal.adapter.in.web;

import com.urgencesante.facility.internal.domain.exception.FacilityNotFoundException;
import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les erreurs du domaine Facility en réponses RFC 9457
 * (application/problem+json). Portée limitée au contrôleur du module.
 */
@RestControllerAdvice(assignableTypes = FacilityController.class)
public class FacilityExceptionHandler {

    @ExceptionHandler(FacilityNotFoundException.class)
    public ProblemDetail handleNotFound(FacilityNotFoundException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Établissement introuvable");
        return problem;
    }

    @ExceptionHandler({FacilityValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Requête invalide");
        return problem;
    }
}
