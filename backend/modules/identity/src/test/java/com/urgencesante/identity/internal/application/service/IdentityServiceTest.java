package com.urgencesante.identity.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.buildingblocks.security.TokenHasher;
import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.PortalCredentialProvisioned;
import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.application.command.ProvisionCredentialCommand;
import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.application.port.out.PortalCredentialEventPublisher;
import com.urgencesante.identity.internal.application.port.out.SaveCredentialPort;
import com.urgencesante.identity.internal.application.port.out.TransactionPort;
import com.urgencesante.identity.internal.domain.exception.IdentityValidationException;
import com.urgencesante.identity.internal.domain.model.PortalCredential;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentityServiceTest {

    private static final UUID FACILITY = UUID.randomUUID();
    private static final String RAW = "operator-secret-token";
    private static final Instant NOW = Instant.parse("2026-08-13T10:00:00Z");

    private List<String> operations;
    private InMemoryCredentials credentials;
    private InMemoryEventPublisher publisher;
    private IdentityService service;

    @BeforeEach
    void setUp() {
        operations = new ArrayList<>();
        credentials = new InMemoryCredentials(operations);
        credentials.seed(new PortalCredential(
                UUID.randomUUID(), "CHU Cocody", TokenHasher.sha256Hex(RAW),
                PortalRole.FACILITY_OPERATOR, FACILITY, true));
        publisher = new InMemoryEventPublisher(operations);
        service = new IdentityService(
                credentials, credentials, new TracingTransactionPort(operations), publisher,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    // ── Authentification ────────────────────────────────────────────────────

    @Test
    void authentifie_un_jeton_valide_et_expose_la_portee() {
        final Optional<PortalPrincipalView> principal = service.authenticate(RAW);

        assertThat(principal).isPresent();
        assertThat(principal.get().role()).isEqualTo(PortalRole.FACILITY_OPERATOR);
        assertThat(principal.get().canActOn(FACILITY)).isTrue();
        assertThat(principal.get().canActOn(UUID.randomUUID())).isFalse();
    }

    @Test
    void rejette_un_jeton_inconnu_vide_ou_nul() {
        assertThat(service.authenticate("mauvais")).isEmpty();
        assertThat(service.authenticate("  ")).isEmpty();
        assertThat(service.authenticate(null)).isEmpty();
    }

    @Test
    void un_admin_agit_sur_tout_etablissement() {
        final PortalPrincipalView admin =
                new PortalPrincipalView(UUID.randomUUID(), "Régulation", PortalRole.ADMIN, null);

        assertThat(admin.canActOn(UUID.randomUUID())).isTrue();
        assertThat(admin.canActOn(FACILITY)).isTrue();
    }

    @Test
    void l_empreinte_est_stable_et_ne_revele_pas_le_jeton() {
        final String hash = TokenHasher.sha256Hex(RAW);

        assertThat(hash).hasSize(64).doesNotContain(RAW);
        assertThat(TokenHasher.sha256Hex(RAW)).isEqualTo(hash);
    }

    // ── Provisioning (issue #164) ───────────────────────────────────────────

    @Test
    void provisionne_un_admin_et_ne_persiste_jamais_le_jeton_en_clair() {
        final NewPortalCredential result = service.provision(
                new ProvisionCredentialCommand("Régulation SAMU", PortalRole.ADMIN, null));

        assertThat(result.rawToken()).isNotBlank();
        final PortalCredential saved = credentials.byId.get(result.id());
        assertThat(saved).isNotNull();
        assertThat(saved.tokenHash()).isEqualTo(TokenHasher.sha256Hex(result.rawToken()));
        assertThat(saved.tokenHash()).doesNotContain(result.rawToken());
        assertThat(saved.role()).isEqualTo(PortalRole.ADMIN);
        assertThat(saved.facilityId()).isNull();
        assertThat(saved.active()).isTrue();
    }

    @Test
    void provisionne_un_operateur_rattache_a_son_etablissement() {
        final NewPortalCredential result = service.provision(
                new ProvisionCredentialCommand("Hôpital X", PortalRole.FACILITY_OPERATOR, FACILITY));

        assertThat(result.role()).isEqualTo(PortalRole.FACILITY_OPERATOR);
        assertThat(result.facilityId()).isEqualTo(FACILITY);
        assertThat(credentials.byId.get(result.id()).facilityId()).isEqualTo(FACILITY);
    }

    @Test
    void refuse_un_operateur_sans_etablissement() {
        assertThatThrownBy(() -> service.provision(
                new ProvisionCredentialCommand("Hôpital X", PortalRole.FACILITY_OPERATOR, null)))
                .isInstanceOf(IdentityValidationException.class)
                .hasMessageContaining("établissement");
    }

    @Test
    void refuse_un_admin_rattache_a_un_etablissement() {
        assertThatThrownBy(() -> service.provision(
                new ProvisionCredentialCommand("Régulation", PortalRole.ADMIN, FACILITY)))
                .isInstanceOf(IdentityValidationException.class)
                .hasMessageContaining("établissement");
    }

    @Test
    void refuse_un_libelle_vide() {
        assertThatThrownBy(() -> service.provision(
                new ProvisionCredentialCommand("  ", PortalRole.ADMIN, null)))
                .isInstanceOf(IdentityValidationException.class);
    }

    @Test
    void le_jeton_provisionne_permet_immediatement_l_authentification() {
        final NewPortalCredential result = service.provision(
                new ProvisionCredentialCommand("Régulation SAMU", PortalRole.ADMIN, null));

        final Optional<PortalPrincipalView> principal = service.authenticate(result.rawToken());

        assertThat(principal).isPresent();
        assertThat(principal.get().id()).isEqualTo(result.id());
    }

    @Test
    void la_persistance_et_la_publication_sont_dans_la_meme_transaction() {
        // Frontière transactionnelle du cas d'usage : tout ou rien, même
        // principe que PatientService#register (issue #130).
        service.provision(new ProvisionCredentialCommand("Régulation SAMU", PortalRole.ADMIN, null));

        assertThat(operations).containsExactly("tx-begin", "save", "publish", "tx-commit");
    }

    @Test
    void l_evenement_publie_reflete_le_credential_sans_jamais_reveler_le_jeton() {
        final NewPortalCredential result = service.provision(
                new ProvisionCredentialCommand("Régulation SAMU", PortalRole.ADMIN, null));

        assertThat(publisher.published).hasSize(1);
        final PortalCredentialProvisioned event = publisher.published.get(0);
        assertThat(event.credentialId()).isEqualTo(result.id());
        assertThat(event.label()).isEqualTo("Régulation SAMU");
        assertThat(event.role()).isEqualTo(PortalRole.ADMIN);
        assertThat(event.occurredAt()).isEqualTo(NOW);
        // PortalCredentialProvisioned ne porte structurellement aucun champ
        // jeton (voir sa déclaration) : aucune assertion supplémentaire n'est
        // nécessaire pour prouver l'absence de fuite.
    }

    // ── Faux ports (substituables — LSP) ─────────────────────────────────────

    private static final class InMemoryCredentials implements LoadCredentialPort, SaveCredentialPort {
        private final Map<UUID, PortalCredential> byId = new HashMap<>();
        private final List<String> operations;

        InMemoryCredentials(List<String> operations) {
            this.operations = operations;
        }

        void seed(PortalCredential credential) {
            byId.put(credential.id(), credential);
        }

        @Override
        public Optional<PortalCredential> findActiveByTokenHash(String tokenHash) {
            return byId.values().stream()
                    .filter(PortalCredential::active)
                    .filter(c -> c.tokenHash().equals(tokenHash))
                    .findFirst();
        }

        @Override
        public void save(PortalCredential credential) {
            operations.add("save");
            byId.put(credential.id(), credential);
        }
    }

    private static final class InMemoryEventPublisher implements PortalCredentialEventPublisher {
        private final List<String> operations;
        private final List<PortalCredentialProvisioned> published = new ArrayList<>();

        InMemoryEventPublisher(List<String> operations) {
            this.operations = operations;
        }

        @Override
        public void publish(PortalCredentialProvisioned event) {
            operations.add("publish");
            published.add(event);
        }
    }

    /** Fausse frontière transactionnelle : trace l'ouverture/fermeture (LSP). */
    private static final class TracingTransactionPort implements TransactionPort {
        private final List<String> operations;

        TracingTransactionPort(List<String> operations) {
            this.operations = operations;
        }

        @Override
        public <T> T inTransaction(Supplier<T> work) {
            operations.add("tx-begin");
            final T result = work.get();
            operations.add("tx-commit");
            return result;
        }
    }
}
