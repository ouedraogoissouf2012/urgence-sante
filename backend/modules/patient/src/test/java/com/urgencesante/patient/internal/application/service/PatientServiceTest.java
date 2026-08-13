package com.urgencesante.patient.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.port.out.LoadPatientPort;
import com.urgencesante.patient.internal.application.port.out.PasswordHasherPort;
import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import com.urgencesante.patient.internal.application.port.out.SavePatientPort;
import com.urgencesante.patient.internal.application.port.out.TransactionPort;
import com.urgencesante.patient.internal.application.result.PatientSession;
import com.urgencesante.patient.internal.domain.exception.InvalidCredentialsException;
import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import com.urgencesante.patient.internal.domain.exception.PhoneAlreadyRegisteredException;
import com.urgencesante.patient.internal.domain.model.PatientAccount;
import com.urgencesante.patient.internal.domain.model.PhoneNumber;
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

class PatientServiceTest {

    private static final UUID FIXED_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-07-27T10:00:00Z");

    private InMemoryPatients patients;
    private FakePasswordHasher hasher;
    private FakeSessions sessions;
    private List<String> operations;
    private TransactionPort transactionPort;
    private PatientService service;

    @BeforeEach
    void setUp() {
        operations = new ArrayList<>();
        patients = new InMemoryPatients(operations);
        hasher = new FakePasswordHasher();
        sessions = new FakeSessions(operations);
        transactionPort = new TracingTransactionPort(operations);
        service = new PatientService(
                patients, patients, hasher, sessions, transactionPort,
                Clock.fixed(NOW, ZoneOffset.UTC),
                () -> FIXED_ID);
    }

    // ── Inscription ─────────────────────────────────────────────────────────

    @Test
    void inscription_cree_un_compte_avec_mot_de_passe_hache() {
        final PatientSession session =
                service.register(new RegisterPatientCommand("+225 0102030405", "Secret123"));

        assertThat(session.patientId()).isEqualTo(FIXED_ID);
        final PatientAccount saved = patients.findByPhone(PhoneNumber.of("+2250102030405")).orElseThrow();
        // Le hash mémorise le mot de passe (vérifiable par matches) sans le stocker en clair.
        assertThat(hasher.matches("Secret123", saved.passwordHash())).isTrue();
        assertThat(saved.passwordHash()).doesNotContain("Secret123");
        assertThat(saved.active()).isTrue();
        assertThat(saved.createdAt()).isEqualTo(NOW);
    }

    @Test
    void inscription_ouvre_la_session_dans_la_meme_transaction_que_la_creation_du_compte() {
        // Issue #130 : compte + session ne doivent plus être commis en deux
        // transactions séparées — la frontière transactionnelle du cas d'usage
        // doit englober save() ET issueToken(), tout ou rien.
        final PatientSession session =
                service.register(new RegisterPatientCommand("+2250102030405", "Secret123"));

        assertThat(operations).containsExactly("tx-begin", "save", "issue-token", "tx-commit");
        assertThat(session.token()).isEqualTo("TOKEN(" + FIXED_ID + ")");
    }

    @Test
    void inscription_normalise_le_telephone() {
        service.register(new RegisterPatientCommand("+225-01.02 03 04 05", "Secret123"));
        assertThat(patients.existsByPhone(PhoneNumber.of("+2250102030405"))).isTrue();
    }

    @Test
    void inscription_refuse_un_numero_deja_pris() {
        service.register(new RegisterPatientCommand("+2250102030405", "Secret123"));

        assertThatThrownBy(() ->
                service.register(new RegisterPatientCommand("+225 0102030405", "Autre123")))
                .isInstanceOf(PhoneAlreadyRegisteredException.class);
    }

    @Test
    void inscription_refuse_un_mot_de_passe_trop_court() {
        assertThatThrownBy(() ->
                service.register(new RegisterPatientCommand("+2250102030405", "abc")))
                .isInstanceOf(PatientValidationException.class)
                .hasMessageContaining("mot de passe");
    }

    @Test
    void inscription_refuse_un_telephone_invalide() {
        assertThatThrownBy(() ->
                service.register(new RegisterPatientCommand("0102030405", "Secret123")))
                .isInstanceOf(PatientValidationException.class);
    }

    // ── Connexion ───────────────────────────────────────────────────────────

    @Test
    void connexion_reussie_ouvre_une_session() {
        service.register(new RegisterPatientCommand("+2250102030405", "Secret123"));

        final PatientSession session = service.login(new LoginCommand("+225 0102030405", "Secret123"));

        assertThat(session.patientId()).isEqualTo(FIXED_ID);
        assertThat(session.token()).isEqualTo("TOKEN(" + FIXED_ID + ")");
    }

    @Test
    void connexion_refuse_un_mauvais_mot_de_passe() {
        service.register(new RegisterPatientCommand("+2250102030405", "Secret123"));

        assertThatThrownBy(() ->
                service.login(new LoginCommand("+2250102030405", "Faux")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void connexion_refuse_un_telephone_inconnu() {
        assertThatThrownBy(() ->
                service.login(new LoginCommand("+2250909090909", "Secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void connexion_refuse_un_compte_inactif() {
        service.register(new RegisterPatientCommand("+2250102030405", "Secret123"));
        patients.deactivate(PhoneNumber.of("+2250102030405"));

        assertThatThrownBy(() ->
                service.login(new LoginCommand("+2250102030405", "Secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void connexion_compare_toujours_un_mot_de_passe_meme_si_le_compte_est_inconnu() {
        // Anti-énumération par timing (audit P3 #140) : si la comparaison
        // BCrypt (coûteuse) n'a lieu QUE pour un compte existant, la latence
        // de la réponse révèle à elle seule si un numéro est enregistré,
        // même avec un message d'erreur unique. La comparaison doit donc
        // avoir lieu à CHAQUE tentative, compte existant ou non.
        assertThatThrownBy(() ->
                service.login(new LoginCommand("+2250909090909", "PeuImporte1")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(hasher.matchesCallCount).isEqualTo(1);
    }

    @Test
    void connexion_compare_toujours_un_mot_de_passe_meme_pour_un_compte_inactif() {
        service.register(new RegisterPatientCommand("+2250102030405", "Secret123"));
        patients.deactivate(PhoneNumber.of("+2250102030405"));
        hasher.matchesCallCount = 0;

        assertThatThrownBy(() ->
                service.login(new LoginCommand("+2250102030405", "Secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(hasher.matchesCallCount).isEqualTo(1);
    }

    // ── Révocation ──────────────────────────────────────────────────────────

    @Test
    void revocation_delegue_au_port_de_sessions_avec_le_meme_jeton() {
        service.revoke("un-jeton-quelconque");

        assertThat(operations).contains("revoke");
        assertThat(sessions.lastRevokedToken).isEqualTo("un-jeton-quelconque");
    }

    // ── Faux ports (substituables — LSP) ─────────────────────────────────────

    private static final class InMemoryPatients implements LoadPatientPort, SavePatientPort {
        private final Map<String, PatientAccount> byPhone = new HashMap<>();
        private final List<String> operations;

        InMemoryPatients(List<String> operations) {
            this.operations = operations;
        }

        @Override
        public Optional<PatientAccount> findByPhone(PhoneNumber phone) {
            return Optional.ofNullable(byPhone.get(phone.value()));
        }

        @Override
        public boolean existsByPhone(PhoneNumber phone) {
            return byPhone.containsKey(phone.value());
        }

        @Override
        public void save(PatientAccount account) {
            operations.add("save");
            byPhone.put(account.phone().value(), account);
        }

        void deactivate(PhoneNumber phone) {
            final PatientAccount a = byPhone.get(phone.value());
            byPhone.put(phone.value(), PatientAccount.restore(
                    a.id(), a.phone(), a.passwordHash(), false, a.createdAt()));
        }
    }

    /**
     * Faux hachage : ne stocke pas le clair (comme BCrypt), mais reste
     * vérifiable par {@link #matches}. On encode l'empreinte du hashCode, ce
     * qui suffit au déterminisme des tests sans exposer le mot de passe.
     * Compte les appels à {@link #matches} : sert à prouver qu'une
     * comparaison a bien lieu même pour un compte inconnu (temps constant).
     */
    private static final class FakePasswordHasher implements PasswordHasherPort {
        int matchesCallCount;

        @Override
        public String hash(String rawPassword) {
            return "bcrypt$" + Integer.toHexString(rawPassword.hashCode());
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            matchesCallCount++;
            return hash(rawPassword).equals(passwordHash);
        }
    }

    private static final class FakeSessions implements PatientSessionPort {
        private final List<String> operations;
        String lastRevokedToken;

        FakeSessions(List<String> operations) {
            this.operations = operations;
        }

        @Override
        public String issueToken(UUID patientId) {
            operations.add("issue-token");
            return "TOKEN(" + patientId + ")";
        }

        @Override
        public Optional<UUID> resolvePatient(String rawToken) {
            return Optional.empty(); // non sollicité par les tests du service
        }

        @Override
        public void revoke(String rawToken) {
            operations.add("revoke");
            lastRevokedToken = rawToken;
        }

        @Override
        public int purgeExpired() {
            return 0; // non sollicité par les tests du service
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
