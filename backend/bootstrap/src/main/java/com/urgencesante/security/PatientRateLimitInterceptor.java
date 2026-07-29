package com.urgencesante.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Limite le débit des inscriptions/connexions patient par IP (anti-abus,
 * anti-force-brute). Ces endpoints sont publics : la clé est l'adresse de
 * transport ({@code getRemoteAddr}, non falsifiable par en-tête). 429 en
 * {@code application/problem+json} au-delà du seuil.
 */
public class PatientRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter attemptsPerIp;

    public PatientRateLimitInterceptor(RateLimiter attemptsPerIp) {
        this.attemptsPerIp = attemptsPerIp;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (attemptsPerIp.tryAcquire("patient-ip:" + request.getRemoteAddr())) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"Trop de requêtes\",\"status\":429,"
                        + "\"detail\":\"Trop de tentatives, réessayez plus tard.\"}");
        return false;
    }
}
