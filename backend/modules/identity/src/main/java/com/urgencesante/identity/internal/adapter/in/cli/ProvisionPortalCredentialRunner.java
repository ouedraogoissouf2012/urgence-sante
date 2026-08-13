package com.urgencesante.identity.internal.adapter.in.cli;

import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.application.command.ProvisionCredentialCommand;
import com.urgencesante.identity.internal.application.port.in.ProvisionPortalCredentialUseCase;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Provisionne un nouveau credential portail au lancement, activé UNIQUEMENT si
 * la propriété {@code identity.provision.enabled=true} EST fournie ET que
 * l'application démarre SANS serveur web (issue #164) — double garde
 * structurelle : une propriété laissée par erreur dans la configuration d'un
 * serveur normal (web) ne peut, à elle seule, jamais déclencher ce
 * provisioning ni fermer le contexte d'un serveur en cours de service.
 *
 * <p>Invocation en processus jetable
 * ({@code --spring.main.web-application-type=none}), voir le runbook
 * docs/operations/PORTAL_CREDENTIAL_PROVISIONING.md. Le jeton en clair est
 * imprimé UNE SEULE FOIS sur stdout (jamais journalisé) ; à charge pour
 * l'opérateur de le transmettre de façon sécurisée puis de fermer ce
 * terminal.
 *
 * <p>Le contexte est fermé EXPLICITEMENT en fin d'exécution réussie
 * ({@link ConfigurableApplicationContext#close()}, jamais {@code System.exit})
 * : laissé vivant, le processus resterait bloqué indéfiniment à cause des
 * threads non-démons des tâches planifiées du serveur
 * ({@code @EnableScheduling}). Un échec (validation du domaine, propriété CLI
 * malformée, etc.) n'a besoin d'AUCUN traitement particulier ici : laissé
 * remonter tel quel, Spring Boot ferme déjà le contexte lui-même avant de
 * faire échouer {@code SpringApplication.run(...)} — un seul mécanisme de
 * sortie, pour toutes les erreurs, plutôt qu'un rattrapage partiel qui
 * n'aurait couvert qu'un sous-ensemble des échecs possibles.
 */
@Component
@ConditionalOnProperty(name = "identity.provision.enabled", havingValue = "true")
@ConditionalOnNotWebApplication
class ProvisionPortalCredentialRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionPortalCredentialRunner.class);

    private final ProvisionPortalCredentialUseCase provisionPortalCredential;
    private final ConfigurableApplicationContext context;
    private final String label;
    private final PortalRole role;
    private final UUID facilityId;

    ProvisionPortalCredentialRunner(
            ProvisionPortalCredentialUseCase provisionPortalCredential,
            ConfigurableApplicationContext context,
            Environment environment) {
        this.provisionPortalCredential = provisionPortalCredential;
        this.context = context;
        this.label = environment.getRequiredProperty("identity.provision.label");
        this.role = PortalRole.valueOf(environment.getRequiredProperty("identity.provision.role"));
        final String rawFacilityId = environment.getProperty("identity.provision.facility-id");
        this.facilityId =
                (rawFacilityId == null || rawFacilityId.isBlank()) ? null : UUID.fromString(rawFacilityId);
    }

    @Override
    public void run(ApplicationArguments args) {
        provision();
        context.close();
    }

    /** Visibilité paquet : testable directement, sans passer par le cycle de vie du contexte. */
    NewPortalCredential provision() {
        final NewPortalCredential credential = provisionPortalCredential.provision(
                new ProvisionCredentialCommand(label, role, facilityId));
        LOG.info(
                "Credential portail provisionné : id={} label={} role={} facilityId={}",
                credential.id(), credential.label(), credential.role(), credential.facilityId());
        System.out.println("TOKEN=" + credential.rawToken());
        return credential;
    }
}
