package com.urgencesante.buildingblocks.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Source unique de construction des réponses d'erreur RFC 9457 pour les
 * {@code @ExceptionHandler} des modules (issue #151) : chaque handler
 * retourne le {@link ProblemDetail} produit ici, que Spring MVC sérialise
 * lui-même via son pipeline normal. Complète {@code ErrorResponses}
 * (bootstrap, issue #148), qui sert les intercepteurs de sécurité — hors de
 * ce pipeline, donc sans {@code @ExceptionHandler} disponible.
 */
public final class ExceptionHandlerSupport {

    private ExceptionHandlerSupport() {
    }

    public static ProblemDetail problemDetail(HttpStatus status, String detail, String title) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
