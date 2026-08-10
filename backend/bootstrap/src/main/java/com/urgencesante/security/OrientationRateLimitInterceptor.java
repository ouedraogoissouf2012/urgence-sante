package com.urgencesante.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Limite le débit de l'orientation par IP (anti-abus, anti-épuisement de
 * threads). Endpoint public et anonyme dont chaque appel peut retenir un
 * thread servlet le temps d'un aller-retour OSRM (issue #121) : sans limite,
 * un volume anormal d'une même IP peut, à lui seul, épuiser le pool de threads
 * et rendre TOUTE l'API indisponible (y compris l'écriture de disponibilité
 * des hôpitaux). La clé est l'adresse de transport ({@code getRemoteAddr}, non
 * falsifiable par en-tête), comme les autres limiteurs du projet.
 */
public class OrientationRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter attemptsPerIp;

    public OrientationRateLimitInterceptor(RateLimiter attemptsPerIp) {
        this.attemptsPerIp = attemptsPerIp;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (attemptsPerIp.tryAcquire("orientation-ip:" + request.getRemoteAddr())) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"Trop de requêtes\",\"status\":429,"
                        + "\"detail\":\"Trop de recherches, réessayez plus tard.\"}");
        return false;
    }
}
