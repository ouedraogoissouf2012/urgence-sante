package com.urgencesante.security;

import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.PortalPrincipalView;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protège la mise à jour de disponibilité (portail hospitalier) : jeton porteur
 * requis, portée par établissement et limitation de débit.
 *
 * <p>Sémantique HTTP : 401 si le jeton est absent ou invalide, 403 si l'agent
 * authentifié n'a pas la portée sur l'établissement visé, 429 si le débit est
 * dépassé. Les endpoints publics (patient) ne sont pas concernés. Les réponses
 * d'erreur sont en {@code application/problem+json} (RFC 9457).
 */
@Component
@Order(10)
public class PortalSecurityFilter extends OncePerRequestFilter {

    /** PUT sur /api/v1/facilities/{facilityId}/availability/{serviceCode}. */
    private static final Pattern UPDATE_PATH = Pattern.compile(
            "^/api/v1/facilities/([^/]+)/availability/[^/]+/?$");

    private final IdentityFacade identityFacade;
    private final RateLimiter authAttemptsPerIp;
    private final RateLimiter updatesPerPrincipal;

    public PortalSecurityFilter(
            IdentityFacade identityFacade,
            RateLimiter authAttemptsPerIp,
            RateLimiter updatesPerPrincipal) {
        this.identityFacade = identityFacade;
        this.authAttemptsPerIp = authAttemptsPerIp;
        this.updatesPerPrincipal = updatesPerPrincipal;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final Matcher matcher = UPDATE_PATH.matcher(request.getRequestURI());
        if (!"PUT".equalsIgnoreCase(request.getMethod()) || !matcher.matches()) {
            chain.doFilter(request, response);
            return;
        }

        // 1) Débit par IP : freine le bourrage de jetons.
        if (!authAttemptsPerIp.tryAcquire("ip:" + clientIp(request))) {
            problem(response, HttpStatus.TOO_MANY_REQUESTS, "Trop de requêtes",
                    "Débit dépassé, réessayez plus tard.");
            return;
        }

        // 2) Authentification par jeton porteur.
        final Optional<PortalPrincipalView> principal =
                bearerToken(request).flatMap(identityFacade::authenticate);
        if (principal.isEmpty()) {
            problem(response, HttpStatus.UNAUTHORIZED, "Non authentifié",
                    "Jeton du portail absent ou invalide.");
            return;
        }

        // 3) Autorisation : portée sur l'établissement visé.
        final UUID targetFacility;
        try {
            targetFacility = UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException invalid) {
            // Identifiant malformé : laissé au contrôleur (400 cohérent).
            chain.doFilter(request, response);
            return;
        }
        if (!principal.get().canActOn(targetFacility)) {
            problem(response, HttpStatus.FORBIDDEN, "Accès refusé",
                    "Cet agent n'est pas autorisé sur cet établissement.");
            return;
        }

        // 4) Débit des mises à jour par agent.
        if (!updatesPerPrincipal.tryAcquire("principal:" + principal.get().label())) {
            problem(response, HttpStatus.TOO_MANY_REQUESTS, "Trop de requêtes",
                    "Trop de mises à jour, réessayez plus tard.");
            return;
        }

        chain.doFilter(request, response);
    }

    private static Optional<String> bearerToken(HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.empty();
        }
        final String token = header.substring(7).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    private static String clientIp(HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static void problem(
            HttpServletResponse response, HttpStatus status, String title, String detail)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        // Jamais de jeton ni de donnée sensible dans la réponse.
        response.getWriter().write(String.format(
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}",
                title, status.value(), detail));
    }
}
