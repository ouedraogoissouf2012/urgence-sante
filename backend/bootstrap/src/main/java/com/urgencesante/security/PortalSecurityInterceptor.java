package com.urgencesante.security;

import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.PortalPrincipalView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Protège la mise à jour de disponibilité (portail hospitalier) : jeton porteur
 * requis, portée par établissement et limitation de débit.
 *
 * <p>Implémenté en {@link HandlerInterceptor} (et non en filtre servlet) pour
 * partager EXACTEMENT la vue du chemin utilisée par le routage Spring : on lit
 * l'identifiant d'établissement dans les variables de gabarit d'URI déjà
 * décodées et normalisées (matrix-params retirés), ce qui élimine toute
 * divergence filtre/dispatcher. La logique ÉCHOUE FERMÉ : tout doute rejette.
 *
 * <p>Sémantique : 401 jeton absent/invalide, 403 hors portée, 429 débit dépassé
 * ({@code application/problem+json}). Le jeton n'est jamais divulgué. Les
 * lectures et les endpoints patient ne sont pas concernés.
 */
public class PortalSecurityInterceptor implements HandlerInterceptor {

    private final IdentityFacade identityFacade;
    private final RateLimiter authAttemptsPerIp;
    private final RateLimiter updatesPerPrincipal;

    public PortalSecurityInterceptor(
            IdentityFacade identityFacade,
            RateLimiter authAttemptsPerIp,
            RateLimiter updatesPerPrincipal) {
        this.identityFacade = identityFacade;
        this.authAttemptsPerIp = authAttemptsPerIp;
        this.updatesPerPrincipal = updatesPerPrincipal;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // Seule l'écriture (PUT) est protégée ; les lectures restent publiques.
        if (!HttpMethod.PUT.matches(request.getMethod())) {
            return true;
        }

        // 1) Débit par IP (adresse de transport, non falsifiable par en-tête).
        if (!authAttemptsPerIp.tryAcquire("ip:" + request.getRemoteAddr())) {
            return reject(response, HttpStatus.TOO_MANY_REQUESTS, "Trop de requêtes",
                    "Débit dépassé, réessayez plus tard.");
        }

        // 2) Authentification par jeton porteur.
        final Optional<PortalPrincipalView> principal =
                bearerToken(request).flatMap(identityFacade::authenticate);
        if (principal.isEmpty()) {
            return reject(response, HttpStatus.UNAUTHORIZED, "Non authentifié",
                    "Jeton du portail absent ou invalide.");
        }

        // 3) Autorisation : portée sur l'établissement RÉELLEMENT routé.
        final Optional<UUID> targetFacility = routedFacilityId(request);
        if (targetFacility.isEmpty()) {
            // Chemin protégé sans identifiant exploitable : rejet (échec fermé).
            return reject(response, HttpStatus.BAD_REQUEST, "Requête invalide",
                    "Identifiant d'établissement absent ou invalide.");
        }
        if (!principal.get().canActOn(targetFacility.get())) {
            return reject(response, HttpStatus.FORBIDDEN, "Accès refusé",
                    "Cet agent n'est pas autorisé sur cet établissement.");
        }

        // 4) Débit des mises à jour par agent (clé = identifiant immuable unique).
        if (!updatesPerPrincipal.tryAcquire("principal:" + principal.get().id())) {
            return reject(response, HttpStatus.TOO_MANY_REQUESTS, "Trop de requêtes",
                    "Trop de mises à jour, réessayez plus tard.");
        }

        return true;
    }

    /** Identifiant d'établissement tel que routé par Spring (décodé, normalisé). */
    @SuppressWarnings("unchecked")
    private static Optional<UUID> routedFacilityId(HttpServletRequest request) {
        final Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attr instanceof Map<?, ?> vars)) {
            return Optional.empty();
        }
        final Object value = ((Map<String, String>) vars).get("facilityId");
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.toString()));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    private static Optional<String> bearerToken(HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.empty();
        }
        final String token = header.substring(7).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    private static boolean reject(
            HttpServletResponse response, HttpStatus status, String title, String detail)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        // Jamais de jeton ni de donnée sensible dans la réponse.
        response.getWriter().write(String.format(
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}",
                title, status.value(), detail));
        return false;
    }
}
