package com.urgencesante.security;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enregistre les interceptors de sécurité : celui du portail (écriture de
 * disponibilité), la limite de débit des inscriptions/connexions patient, et
 * celle de l'orientation. Séparé de {@link SecurityConfiguration} pour
 * injecter les beans sans cycle d'auto-référence.
 */
@Configuration
class PortalSecurityWebConfig implements WebMvcConfigurer {

    private final PortalSecurityInterceptor interceptor;
    private final PatientRateLimitInterceptor patientRateLimit;
    private final OrientationRateLimitInterceptor orientationRateLimit;

    PortalSecurityWebConfig(
            PortalSecurityInterceptor interceptor,
            PatientRateLimitInterceptor patientRateLimit,
            OrientationRateLimitInterceptor orientationRateLimit) {
        this.interceptor = interceptor;
        this.patientRateLimit = patientRateLimit;
        this.orientationRateLimit = orientationRateLimit;
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
        // Orientation (GET public, anonyme) : anti-abus / anti-épuisement de
        // threads — l'appel OSRM en aval retient un thread servlet (issue #121).
        registry.addInterceptor(orientationRateLimit)
                .addPathPatterns("/api/v1/orientation");
    }
}
