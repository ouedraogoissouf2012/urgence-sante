package com.urgencesante.orientation.internal.adapter.in.web;

import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduit les erreurs du domaine en réponses RFC 9457, limité à ce contrôleur. */
@RestControllerAdvice(assignableTypes = OrientationController.class)
public class OrientationExceptionHandler {

    @ExceptionHandler({OrientationValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Requête invalide");
        return problem;
    }
}
