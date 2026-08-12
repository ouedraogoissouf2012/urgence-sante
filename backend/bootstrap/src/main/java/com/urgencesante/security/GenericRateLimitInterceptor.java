package com.urgencesante.security;

import com.urgencesante.config.ErrorResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Limite le débit d'un endpoint public par IP (anti-abus, anti-force-brute,
 * anti-épuisement de threads). La clé et le message sont paramétrés au lieu
 * d'être dupliqués par endpoint (issue #147 : {@code PatientRateLimitInterceptor}
 * et {@code OrientationRateLimitInterceptor} étaient identiques à 95 %). La
 * clé est l'adresse de transport ({@code getRemoteAddr}, non falsifiable par
 * en-tête). 429 en {@code application/problem+json}, sérialisé via {@link
 * ErrorResponses} pour rester au même format que les exception handlers
 * (issue #148).
 */
public final class GenericRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter attemptsPerIp;
    private final String rateLimiterKeyPrefix;
    private final ErrorResponses errorResponses;
    private final String tooManyRequestsDetail;

    public GenericRateLimitInterceptor(
            RateLimiter attemptsPerIp,
            String rateLimiterKeyPrefix,
            ErrorResponses errorResponses,
            String tooManyRequestsDetail) {
        this.attemptsPerIp = attemptsPerIp;
        this.rateLimiterKeyPrefix = rateLimiterKeyPrefix;
        this.errorResponses = errorResponses;
        this.tooManyRequestsDetail = tooManyRequestsDetail;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (attemptsPerIp.tryAcquire(rateLimiterKeyPrefix + request.getRemoteAddr())) {
            return true;
        }
        errorResponses.writeProblem(
                request, response, HttpStatus.TOO_MANY_REQUESTS, "Trop de requêtes", tooManyRequestsDetail);
        return false;
    }
}
