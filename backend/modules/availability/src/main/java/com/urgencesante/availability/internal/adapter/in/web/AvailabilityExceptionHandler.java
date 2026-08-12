package com.urgencesante.availability.internal.adapter.in.web;

import com.urgencesante.availability.internal.domain.exception.AvailabilityValidationException;
import com.urgencesante.availability.internal.domain.exception.ServiceNotOfferedException;
import com.urgencesante.buildingblocks.web.ExceptionHandlerSupport;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduit les erreurs du domaine en réponses RFC 9457, limité à ce contrôleur. */
@RestControllerAdvice(assignableTypes = AvailabilityController.class)
public class AvailabilityExceptionHandler {

    @ExceptionHandler({AvailabilityValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage(), "Requête invalide");
    }

    @ExceptionHandler(ServiceNotOfferedException.class)
    public ProblemDetail handleNotOffered(ServiceNotOfferedException exception) {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(),
                "Service non offert par cet établissement");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.CONFLICT, "Cette ressource existe déjà ou viole une contrainte d'unicité",
                "Conflit de contrainte");
    }
}
