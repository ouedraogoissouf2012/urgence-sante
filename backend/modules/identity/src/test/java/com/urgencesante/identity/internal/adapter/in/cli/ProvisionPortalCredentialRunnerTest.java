package com.urgencesante.identity.internal.adapter.in.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.application.command.ProvisionCredentialCommand;
import com.urgencesante.identity.internal.application.port.in.ProvisionPortalCredentialUseCase;
import com.urgencesante.identity.internal.domain.exception.IdentityValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@code run()}/la fermeture du contexte ne sont pas testés ici — pattern déjà
 * établi dans ce dépôt pour les ApplicationRunner de démarrage
 * (FacilityImportRunner, DemoDataProductionGuard, ni l'un ni l'autre testés
 * directement) : seule la logique propre à ce composant (lecture des
 * propriétés, délégation au cas d'usage) est testée, via la méthode paquet
 * {@link ProvisionPortalCredentialRunner#provision()}. Le constructeur reçoit
 * {@code null} pour le contexte Spring : {@code provision()} ne le touche
 * jamais, seul {@code run()} (non exercé ici) en a besoin.
 */
class ProvisionPortalCredentialRunnerTest {

    private static final UUID FACILITY = UUID.randomUUID();

    @Test
    void delegue_au_cas_d_usage_avec_les_proprietes_lues() {
        final MockEnvironment env = new MockEnvironment()
                .withProperty("identity.provision.label", "Régulation SAMU")
                .withProperty("identity.provision.role", "ADMIN");
        final List<ProvisionCredentialCommand> received = new ArrayList<>();
        final ProvisionPortalCredentialUseCase useCase = command -> {
            received.add(command);
            return new NewPortalCredential(
                    UUID.randomUUID(), command.label(), command.role(), command.facilityId(), "raw-token");
        };
        final ProvisionPortalCredentialRunner runner = new ProvisionPortalCredentialRunner(useCase, null, env);

        final NewPortalCredential result = runner.provision();

        assertThat(received).containsExactly(
                new ProvisionCredentialCommand("Régulation SAMU", PortalRole.ADMIN, null));
        assertThat(result.rawToken()).isEqualTo("raw-token");
    }

    @Test
    void lit_l_etablissement_optionnel_pour_un_operateur() {
        final MockEnvironment env = new MockEnvironment()
                .withProperty("identity.provision.label", "Hôpital X")
                .withProperty("identity.provision.role", "FACILITY_OPERATOR")
                .withProperty("identity.provision.facility-id", FACILITY.toString());
        final List<ProvisionCredentialCommand> received = new ArrayList<>();
        final ProvisionPortalCredentialUseCase useCase = command -> {
            received.add(command);
            return new NewPortalCredential(
                    UUID.randomUUID(), command.label(), command.role(), command.facilityId(), "raw-token");
        };
        final ProvisionPortalCredentialRunner runner = new ProvisionPortalCredentialRunner(useCase, null, env);

        runner.provision();

        assertThat(received).containsExactly(
                new ProvisionCredentialCommand("Hôpital X", PortalRole.FACILITY_OPERATOR, FACILITY));
    }

    @Test
    void un_etablissement_vide_est_traite_comme_absent_plutot_que_de_faire_planter_le_parsing_uuid() {
        // Un modèle produisant --identity.provision.facility-id= (variable shell
        // non définie) donne une propriété VIDE, pas ABSENTE : sans ce garde,
        // UUID.fromString("") lève dans le constructeur. Traiter vide comme
        // absent laisse la validation du domaine (IdentityValidationException,
        // message clair) rejeter le cas FACILITY_OPERATOR sans établissement,
        // plutôt qu'un IllegalArgumentException de parsing UUID opaque.
        final MockEnvironment env = new MockEnvironment()
                .withProperty("identity.provision.label", "Hôpital X")
                .withProperty("identity.provision.role", "FACILITY_OPERATOR")
                .withProperty("identity.provision.facility-id", "");
        final ProvisionPortalCredentialUseCase useCase = command -> {
            throw new IdentityValidationException("établissement requis");
        };
        final ProvisionPortalCredentialRunner runner = new ProvisionPortalCredentialRunner(useCase, null, env);

        assertThatThrownBy(runner::provision).isInstanceOf(IdentityValidationException.class);
    }

    @Test
    void un_echec_du_cas_d_usage_remonte_sans_etre_avale() {
        // Aucun rattrapage local : provision() laisse toute erreur (validation
        // du domaine ou autre) remonter telle quelle — c'est run() qui laisse
        // Spring Boot gérer l'échec de façon uniforme (voir la Javadoc de la
        // classe), donc rien ne doit intercepter ou transformer l'exception ici.
        final MockEnvironment env = new MockEnvironment()
                .withProperty("identity.provision.label", "Hôpital X")
                .withProperty("identity.provision.role", "FACILITY_OPERATOR")
                .withProperty("identity.provision.facility-id", FACILITY.toString());
        final ProvisionPortalCredentialUseCase useCase = command -> {
            throw new IdentityValidationException("échec simulé");
        };
        final ProvisionPortalCredentialRunner runner = new ProvisionPortalCredentialRunner(useCase, null, env);

        assertThatThrownBy(runner::provision)
                .isInstanceOf(IdentityValidationException.class)
                .hasMessage("échec simulé");
    }
}
