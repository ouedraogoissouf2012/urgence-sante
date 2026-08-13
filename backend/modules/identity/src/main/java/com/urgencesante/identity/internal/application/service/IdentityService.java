package com.urgencesante.identity.internal.application.service;

import com.urgencesante.buildingblocks.security.OpaqueTokenGenerator;
import com.urgencesante.buildingblocks.security.TokenHasher;
import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.PortalCredentialProvisioned;
import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.internal.application.command.ProvisionCredentialCommand;
import com.urgencesante.identity.internal.application.port.in.AuthenticatePortalUseCase;
import com.urgencesante.identity.internal.application.port.in.ProvisionPortalCredentialUseCase;
import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.application.port.out.PortalCredentialEventPublisher;
import com.urgencesante.identity.internal.application.port.out.SaveCredentialPort;
import com.urgencesante.identity.internal.application.port.out.TransactionPort;
import com.urgencesante.identity.internal.domain.model.PortalCredential;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentifie un jeton du portail : calcule l'empreinte du jeton présenté et
 * cherche un identifiant actif correspondant. Aucun jeton en clair n'est
 * conservé ni comparé en mémoire au-delà du hachage.
 *
 * <p>Provisionne aussi de nouveaux identifiants portail (issue #164) : génère
 * un jeton opaque haute entropie ({@link OpaqueTokenGenerator}, même schéma
 * que les jetons de session patient), ne persiste que son empreinte, et
 * publie un événement d'audit — persistance et publication sont atomiques
 * (même transaction), même principe que {@code AvailabilityService#update}.
 */
public class IdentityService implements AuthenticatePortalUseCase, ProvisionPortalCredentialUseCase {

    private final LoadCredentialPort loadCredentialPort;
    private final SaveCredentialPort saveCredentialPort;
    private final TransactionPort transactionPort;
    private final PortalCredentialEventPublisher eventPublisher;
    private final Clock clock;

    public IdentityService(
            LoadCredentialPort loadCredentialPort,
            SaveCredentialPort saveCredentialPort,
            TransactionPort transactionPort,
            PortalCredentialEventPublisher eventPublisher,
            Clock clock) {
        this.loadCredentialPort = Objects.requireNonNull(loadCredentialPort);
        this.saveCredentialPort = Objects.requireNonNull(saveCredentialPort);
        this.transactionPort = Objects.requireNonNull(transactionPort);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Optional<PortalPrincipalView> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        final String tokenHash = TokenHasher.sha256Hex(rawToken.trim());
        return loadCredentialPort.findActiveByTokenHash(tokenHash)
                .map(IdentityService::toPrincipal);
    }

    @Override
    public NewPortalCredential provision(ProvisionCredentialCommand command) {
        Objects.requireNonNull(command, "command");
        final String rawToken = OpaqueTokenGenerator.generate();
        final String tokenHash = TokenHasher.sha256Hex(rawToken);
        final PortalCredential credential = PortalCredential.provision(
                UUID.randomUUID(), command.label(), tokenHash, command.role(), command.facilityId());

        return transactionPort.inTransaction(() -> {
            saveCredentialPort.save(credential);
            eventPublisher.publish(new PortalCredentialProvisioned(
                    UUID.randomUUID(),
                    credential.id(),
                    credential.label(),
                    credential.role(),
                    credential.facilityId(),
                    clock.instant()));
            return new NewPortalCredential(
                    credential.id(), credential.label(), credential.role(), credential.facilityId(), rawToken);
        });
    }

    private static PortalPrincipalView toPrincipal(PortalCredential credential) {
        return new PortalPrincipalView(
                credential.id(), credential.label(), credential.role(), credential.facilityId());
    }
}
