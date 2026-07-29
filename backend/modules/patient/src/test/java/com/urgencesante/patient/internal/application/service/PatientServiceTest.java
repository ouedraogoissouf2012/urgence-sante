package com.urgencesante.patient.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.port.out.LoadPatientPort;
import com.urgencesante.patient.internal.application.port.out.PasswordHasherPort;
import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import com.urgencesante.patient.internal.application.port.out.SavePatientPort;
import com.urgencesante.patient.internal.application.result.PatientSession;
import com.urgencesante.patient.internal.domain.exception.InvalidCredentialsException;
import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import com.urgencesante.patient.internal.domain.exception.PhoneAlreadyRegisteredException;
import com.urgencesante.patient.internal.domain.model.PatientAccount;
import com.urgencesante.patient.internal.domain.model.PhoneNumber;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PatientServiceTest {

    private static final UUID FIXED_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-07-27T10:00:00Z");

    private InMemoryPatients patients;
    private FakePasswordHasher hasher;
    private FakeSessions sessions;
    private PatientService service;

    @BeforeEach
    void setUp() {
        patients = new InMemoryPatients();
        hasher = new FakePasswordHasher();
        sessions = new FakeSessions();
        service = new PatientService(
                patients, patients, hasher, sessions,
                Clock.fixed(NOW, ZoneOffset.UTC),
                () -> FIXED_ID);
    }

    // ── Inscription ─────────────────────────────────────────────────────────

    @Test
    void inscription_cree_un_compte_avec_mot_de_passe_hache() {
        final UUID id = service.register(new RegisterPatientCommand("+225 0102030405", "Secret123"));

        assertThat(id).isEqualTo(FIXED_ID);
        final PatientAccount saved = patients.findByPhone(PhoneNumber.of("+2250102030405")).orElseThrow();
        // Le hash mémorise le mot de passe (vérifiable par matches) sans le stocker en clair.
        assertThat(hasher.matches("Secret123", saved.passwordHash())).isTrue();
        assertThat(saved.passwordHash()).doesNotContain("Secret123");
        assertThat(saved.active()).isTrue();
        assertThat(saved.createdAt()).isEqualTo(NOW);
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

    // ── Faux ports (substituables — LSP) ─────────────────────────────────────

    private static final class InMemoryPatients implements LoadPatientPort, SavePatientPort {
        private final Map<String, PatientAccount> byPhone = new HashMap<>();

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
     */
    private static final class FakePasswordHasher implements PasswordHasherPort {
        @Override
        public String hash(String rawPassword) {
            return "bcrypt$" + Integer.toHexString(rawPassword.hashCode());
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return hash(rawPassword).equals(passwordHash);
        }
    }

    private static final class FakeSessions implements PatientSessionPort {
        @Override
        public String issueToken(UUID patientId) {
            return "TOKEN(" + patientId + ")";
        }

        @Override
        public Optional<UUID> resolvePatient(String rawToken) {
            return Optional.empty(); // non sollicité par les tests du service
        }
    }
}
