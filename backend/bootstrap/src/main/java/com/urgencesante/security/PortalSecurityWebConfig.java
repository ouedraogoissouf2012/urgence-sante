package com.urgencesante.security;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enregistre l'interceptor de sécurité du portail sur l'écriture de
 * disponibilité. Séparé de {@link SecurityConfiguration} pour injecter le
 * bean interceptor sans cycle d'auto-référence.
 */
@Configuration
class PortalSecurityWebConfig implements WebMvcConfigurer {

    private final PortalSecurityInterceptor interceptor;

    PortalSecurityWebConfig(PortalSecurityInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Motif routé par Spring : un segment établissement, un segment service.
        // La restriction à la méthode PUT est faite dans l'interceptor.
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/v1/facilities/*/availability/*");
    }
}
