package com.urgencesante.config;

import com.urgencesante.buildingblocks.web.ExceptionHandlerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Erreurs de liaison de paramètres du FRAMEWORK (audit P3 #140, point 7) : ex.
 * {@code lat=abc} sur un {@code @RequestParam double}, un paramètre requis
 * absent, un corps JSON illisible. Ce ne sont ni des exceptions du domaine ni
 * interceptées par un des 6 {@code *ExceptionHandler} par module (issue #151)
 * — avant ce filet, elles tombaient sur le {@code BasicErrorController} par
 * défaut de Spring Boot, hors format {@code application/problem+json}.
 *
 * <p>{@code @RestControllerAdvice} GLOBAL (aucun {@code assignableTypes}),
 * donc applicable à tout contrôleur. SANS RISQUE d'ombrager un handler de
 * module plus spécifique : aucun des trois types ci-dessous n'est déclaré par
 * un autre {@code @ControllerAdvice} de ce dépôt — contrairement à un
 * {@code @ExceptionHandler(Exception.class)} générique (qui capterait TOUTE
 * exception, y compris celles déjà gérées ailleurs), Spring ne résout un type
 * d'exception dans qu'UN SEUL bean advice : le premier, dans l'ordre de
 * découverte, qui déclare CE type précis — sans comparer la spécificité entre
 * beans. D'où le filet du point 4 (erreurs vraiment inattendues) implémenté à
 * part, en {@link LastResortExceptionResolver}, à un niveau où Spring garantit
 * qu'il ne s'exécute qu'après épuisement de TOUS les {@code @ExceptionHandler}
 * (les 6 modules ET ce filet-ci compris).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.BAD_REQUEST,
                "Paramètre '" + exception.getName() + "' invalide : " + exception.getValue(),
                "Requête invalide");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException exception) {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.BAD_REQUEST,
                "Paramètre requis absent : " + exception.getParameterName(),
                "Requête invalide");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody() {
        return ExceptionHandlerSupport.problemDetail(
                HttpStatus.BAD_REQUEST,
                "Corps de requête illisible ou mal formé.",
                "Requête invalide");
    }
}
