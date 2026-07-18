package com.urgencesante.routing.internal.adapter.in.web;

import com.urgencesante.routing.internal.domain.exception.RouteNotFoundException;
import com.urgencesante.routing.internal.domain.exception.RoutingValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduit les erreurs du domaine en réponses RFC 9457, limité à ce contrôleur. */
@RestControllerAdvice(assignableTypes = RoutingController.class)
public class RoutingExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ProblemDetail handleNotFound(RouteNotFoundException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Itinéraire introuvable");
        return problem;
    }

    @ExceptionHandler({RoutingValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Requête invalide");
        return problem;
    }
}
