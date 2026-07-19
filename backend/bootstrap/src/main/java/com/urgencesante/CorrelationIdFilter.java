package com.urgencesante;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Corrélation de requête : réutilise l'en-tête {@code X-Correlation-Id} du
 * client (validé) ou en génère un, l'expose dans le MDC (présent dans chaque
 * ligne de log) et le renvoie dans la réponse. Aucune donnée sensible (jamais
 * de position) n'est journalisée par ce mécanisme.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    /** Format accepté pour un identifiant fourni par le client. */
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{8,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String provided = request.getHeader(HEADER);
        final String correlationId = provided != null && SAFE_ID.matcher(provided).matches()
                ? provided
                : UUID.randomUUID().toString();
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
