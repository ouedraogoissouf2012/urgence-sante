package com.urgencesante.identity.internal.configuration;

import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.application.port.out.PortalCredentialEventPublisher;
import com.urgencesante.identity.internal.application.port.out.SaveCredentialPort;
import com.urgencesante.identity.internal.application.port.out.TransactionPort;
import com.urgencesante.identity.internal.application.service.IdentityService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage Spring du module Identity. L'horloge applicative est fournie par
 * l'assemblage global (bootstrap).
 */
@Configuration
public class IdentityConfiguration {

    @Bean
    IdentityService identityService(
            LoadCredentialPort loadCredentialPort,
            SaveCredentialPort saveCredentialPort,
            TransactionPort transactionPort,
            PortalCredentialEventPublisher eventPublisher,
            Clock clock) {
        return new IdentityService(loadCredentialPort, saveCredentialPort, transactionPort, eventPublisher, clock);
    }
}
