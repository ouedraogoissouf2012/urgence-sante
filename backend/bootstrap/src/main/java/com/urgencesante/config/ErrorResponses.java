package com.urgencesante.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Source unique de sérialisation ET d'écriture des réponses d'erreur RFC 9457
 * (issue #148). Les {@code @ExceptionHandler} des modules retournent un
 * {@link ProblemDetail} et laissent Spring MVC le sérialiser via l'{@link
 * ObjectMapper} applicatif. Les intercepteurs de sécurité, eux, écrivent la
 * réponse d'erreur AVANT le pipeline Spring MVC (dans {@code preHandle}) et
 * n'ont donc ni {@code @ExceptionHandler} ni {@code
 * MappingJackson2HttpMessageConverter} disponibles : {@link
 * #writeProblem} centralise pour eux la construction du {@link ProblemDetail}
 * et l'écriture sur la réponse, pour ne PAS dupliquer cette séquence dans
 * chaque intercepteur (source d'un ancien bug : JSON composé à la main, sans
 * échappement).
 */
@Component
public class ErrorResponses {

    private final ObjectMapper objectMapper;

    public ErrorResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(ProblemDetail problem) throws IOException {
        return objectMapper.writeValueAsString(problem);
    }

    public void writeProblem(
            HttpServletRequest request, HttpServletResponse response,
            HttpStatus status, String title, String detail)
            throws IOException {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        // Spring MVC peuple "instance" avec le chemin de la requête pour tout
        // ProblemDetail renvoyé par un @ExceptionHandler ; on reproduit ce
        // comportement ici pour un contrat identique. Repli sûr si le chemin
        // brut n'est pas un URI valide (RFC 9457 : "instance" est optionnel) —
        // sinon URI.create lève AVANT que le statut d'erreur ne soit écrit,
        // et le rejet voulu (401/403/429...) se transforme en exception non
        // gérée.
        problem.setInstance(safeInstance(request));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        // getWriter() encode par défaut en ISO-8859-1 (défaut servlet), à la
        // différence de MappingJackson2HttpMessageConverter qui écrit
        // toujours en UTF-8 : sans ceci, un message accentué ("réessayez")
        // serait mal encodé ici mais correct côté exception handler — brisant
        // le contrat unifié que cette classe existe pour garantir.
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(toJson(problem));
    }

    private static URI safeInstance(HttpServletRequest request) {
        try {
            return URI.create(request.getRequestURI());
        } catch (IllegalArgumentException malformed) {
            return null;
        }
    }
}
