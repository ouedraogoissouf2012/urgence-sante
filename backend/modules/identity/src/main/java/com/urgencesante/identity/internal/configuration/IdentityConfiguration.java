package com.urgencesante.identity.internal.configuration;

import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.application.service.IdentityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Assemblage Spring du module Identity. */
@Configuration
public class IdentityConfiguration {

    @Bean
    IdentityService identityService(LoadCredentialPort loadCredentialPort) {
        return new IdentityService(loadCredentialPort);
    }
}
