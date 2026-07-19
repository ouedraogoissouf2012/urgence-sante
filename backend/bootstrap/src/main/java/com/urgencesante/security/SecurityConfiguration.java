package com.urgencesante.security;

import com.urgencesante.identity.IdentityFacade;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage de la sécurité du portail : limiteurs de débit (horloge injectée)
 * et {@link PortalSecurityInterceptor}. L'enregistrement MVC est fait par
 * {@link PortalSecurityWebConfig}. Valeurs configurables par l'environnement.
 */
@Configuration
public class SecurityConfiguration {

    /** Tentatives (dont authentification) par IP sur l'endpoint sensible. */
    @Bean
    RateLimiter authAttemptsPerIp(
            Clock clock,
            @Value("${security.portal.rate.auth-per-ip:20}") int capacity,
            @Value("${security.portal.rate.window-ms:60000}") long windowMs) {
        return new RateLimiter(capacity, Duration.ofMillis(windowMs), clock);
    }

    /** Mises à jour par agent authentifié. */
    @Bean
    RateLimiter updatesPerPrincipal(
            Clock clock,
            @Value("${security.portal.rate.updates-per-principal:60}") int capacity,
            @Value("${security.portal.rate.window-ms:60000}") long windowMs) {
        return new RateLimiter(capacity, Duration.ofMillis(windowMs), clock);
    }

    @Bean
    PortalSecurityInterceptor portalSecurityInterceptor(
            IdentityFacade identityFacade,
            @Qualifier("authAttemptsPerIp") RateLimiter authAttemptsPerIp,
            @Qualifier("updatesPerPrincipal") RateLimiter updatesPerPrincipal) {
        return new PortalSecurityInterceptor(
                identityFacade, authAttemptsPerIp, updatesPerPrincipal);
    }
}
