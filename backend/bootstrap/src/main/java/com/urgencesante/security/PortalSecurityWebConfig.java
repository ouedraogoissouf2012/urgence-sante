package com.urgencesante.security;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enregistre les interceptors de sécurité : celui du portail (écriture de
 * disponibilité) et la limite de débit des inscriptions/connexions patient.
 * Séparé de {@link SecurityConfiguration} pour injecter les beans sans cycle
 * d'auto-référence.
 */
@Configuration
class PortalSecurityWebConfig implements WebMvcConfigurer {

    private final PortalSecurityInterceptor interceptor;
    private final PatientRateLimitInterceptor patientRateLimit;

    PortalSecurityWebConfig(
            PortalSecurityInterceptor interceptor, PatientRateLimitInterceptor patientRateLimit) {
        this.interceptor = interceptor;
        this.patientRateLimit = patientRateLimit;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Motif routé par Spring : un segment établissement, un segment service.
        // La restriction à la méthode PUT est faite dans l'interceptor.
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/v1/facilities/*/availability/*");
        // Inscription et connexion patient (POST publics) : anti-abus par IP.
        registry.addInterceptor(patientRateLimit)
                .addPathPatterns("/api/v1/patients", "/api/v1/patients/session");
    }
}
