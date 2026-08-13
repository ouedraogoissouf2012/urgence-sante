package com.urgencesante.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Filet de sécurité de DERNIER RECOURS pour toute exception vraiment
 * INATTENDUE (audit P3 #140, point 4) : jamais son message n'est renvoyé au
 * client (fuite d'information potentielle — requête SQL, chemin de classe…),
 * toujours journalisée à ERROR (avec l'identifiant de corrélation, déjà
 * présent dans le MDC par {@link com.urgencesante.CorrelationIdFilter}) pour
 * rester diagnosticable côté serveur.
 *
 * <p>Délibérément un {@link HandlerExceptionResolver} plutôt qu'un
 * {@code @ExceptionHandler(Exception.class)} dans un {@code @RestControllerAdvice} :
 * Spring résout un {@code @ExceptionHandler} en choisissant le PREMIER bean
 * advice (dans l'ordre de découverte, PAS de spécificité) qui déclare le type
 * lancé — un handler générique {@code Exception.class} placé dans un advice
 * peut donc ombrager, selon l'ordre de câblage (non garanti), un handler plus
 * spécifique d'un AUTRE bean advice (les 6 {@code *ExceptionHandler} par
 * module, issue #151). Un {@link HandlerExceptionResolver} séparé, en
 * {@link Ordered#LOWEST_PRECEDENCE}, s'exécute dans la chaîne de résolution
 * Spring MVC STRICTEMENT APRÈS {@code ExceptionHandlerExceptionResolver} (qui
 * traite TOUS les {@code @ExceptionHandler}, modules et {@link
 * GlobalExceptionHandler} compris) — donc uniquement quand rien d'autre n'a
 * intercepté l'exception. Réutilise {@link ErrorResponses}, déjà responsable
 * (issue #148) d'écrire une réponse RFC 9457 hors du pipeline
 * {@code @ExceptionHandler} standard.
 */
@Component
public class LastResortExceptionResolver implements HandlerExceptionResolver, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LastResortExceptionResolver.class);

    private final ErrorResponses errorResponses;

    public LastResortExceptionResolver(ErrorResponses errorResponses) {
        this.errorResponses = errorResponses;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        log.error("Erreur non gérée sur {} {}", request.getMethod(), request.getRequestURI(), exception);
        // Dernier recours : rien en aval ne rattrape ce resolver lui-même s'il
        // lève. Une réponse déjà committée (ex. flux partiellement écrit) rend
        // toute écriture supplémentaire vaine ou levante (IllegalStateException,
        // pas une IOException) — on s'arrête proprement plutôt que de risquer
        // une exception non gérée échappant au dernier filet lui-même.
        if (response.isCommitted()) {
            log.error("Réponse déjà committée : impossible d'écrire le ProblemDetail de secours.");
            return new ModelAndView();
        }
        try {
            errorResponses.writeProblem(
                    request, response, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur interne", "Une erreur inattendue s'est produite. Réessayez plus tard.");
        } catch (Exception writeFailed) {
            log.error("Échec d'écriture de la réponse d'erreur elle-même", writeFailed);
        }
        return new ModelAndView();
    }
}
