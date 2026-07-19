package com.urgencesante;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active l'ordonnanceur de tâches (relais d'outbox de disponibilité, etc.).
 * Désactivé en profil test : les tests pilotent le relais explicitement
 * (relayOnce) pour des assertions déterministes.
 */
@Configuration
@EnableScheduling
@org.springframework.context.annotation.Profile("!test")
public class SchedulingConfiguration {
}
