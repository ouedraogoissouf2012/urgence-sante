package com.urgencesante;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Applique la politique CORS à l'API. Préoccupation d'assemblage :
 * les origines proviennent de la configuration (CORS_ALLOWED_ORIGINS),
 * aucune origine n'est autorisée par défaut, et les motifs génériques sont
 * rejetés au démarrage en production (voir {@link CorsPolicy}).
 */
@Configuration
public class CorsWebConfiguration {

    @Bean
    CorsPolicy corsPolicy(
            @Value("${app.cors.allowed-origin-patterns:}") String rawPatterns,
            Environment environment) {
        final boolean production = environment.matchesProfiles("production");
        return CorsPolicy.of(rawPatterns, production);
    }

    @Bean
    WebMvcConfigurer corsConfigurer(CorsPolicy policy) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (!policy.isEnabled()) {
                    return;
                }
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(policy.allowedOriginPatterns().toArray(String[]::new))
                        .allowedMethods("GET", "PUT", "POST", "DELETE")
                        .maxAge(3600);
            }
        };
    }
}
