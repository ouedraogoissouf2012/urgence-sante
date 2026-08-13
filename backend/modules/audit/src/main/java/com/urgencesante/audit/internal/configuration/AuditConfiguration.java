package com.urgencesante.audit.internal.configuration;

import com.urgencesante.audit.internal.application.port.out.SaveAuditEntryPort;
import com.urgencesante.audit.internal.application.service.AuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage Spring du module Audit. Le service applicatif reste en Java pur ;
 * il est exposé comme bean ici, câblé au port sortant fourni par l'adaptateur
 * de persistance.
 */
@Configuration
public class AuditConfiguration {

    @Bean
    AuditService auditService(SaveAuditEntryPort saveAuditEntryPort) {
        return new AuditService(saveAuditEntryPort);
    }
}
