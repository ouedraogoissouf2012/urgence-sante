package com.urgencesante.routing.internal.adapter.in.web;

import com.urgencesante.buildingblocks.web.ExceptionHandlerSupport;
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
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.NOT_FOUND, exception.getMessage(), "Itinéraire introuvable");
    }

    @ExceptionHandler({RoutingValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage(), "Requête invalide");
    }
}
