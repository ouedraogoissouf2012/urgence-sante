package com.urgencesante;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge applicative unique, injectable partout (availability, routing…).
 * Fournie à l'assemblage pour éviter tout couplage entre modules métier.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }
}
