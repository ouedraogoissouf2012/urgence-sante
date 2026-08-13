package com.urgencesante.patient.internal.application.service;

import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.port.in.AuthenticatePatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RegisterPatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RevokePatientSessionUseCase;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Cœur applicatif des comptes patients : inscription et authentification.
 *
 * <p>POJO sans annotation Spring (câblé dans la configuration). Dépend
 * uniquement de ports — donc testable en isolation avec des faux. L'{@code id}
 * et l'horloge sont injectés (déterminisme des tests).
 */
public class PatientService
        implements RegisterPatientUseCase, AuthenticatePatientUseCase, RevokePatientSessionUseCase {

    /** Longueur minimale d'un mot de passe (borne serveur, non négociable). */
    static final int MIN_PASSWORD_LENGTH = 8;

    private final LoadPatientPort loadPatient;
    private final SavePatientPort savePatient;
    private final PasswordHasherPort passwordHasher;
    private final PatientSessionPort sessions;
    private final TransactionPort transactionPort;
    private final Clock clock;
    private final Supplier<UUID> idGenerator;

    /**
     * Empreinte factice (calculée une fois, coût BCrypt identique à une vraie
     * empreinte) : contre l'énumération de comptes par timing (audit P3
     * #140). Sans elle, {@link #login} ne comparait via BCrypt (~100 ms) QUE
     * si le compte existait — un compte inconnu échouait en microsecondes,
     * rendant l'existence d'un numéro observable par la seule latence de la
     * réponse, malgré un message d'erreur unique.
     */
    private final String dummyPasswordHash;

    public PatientService(
            LoadPatientPort loadPatient,
            SavePatientPort savePatient,
            PasswordHasherPort passwordHasher,
            PatientSessionPort sessions,
            TransactionPort transactionPort,
            Clock clock,
            Supplier<UUID> idGenerator) {
        this.loadPatient = Objects.requireNonNull(loadPatient);
        this.savePatient = Objects.requireNonNull(savePatient);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.sessions = Objects.requireNonNull(sessions);
        this.transactionPort = Objects.requireNonNull(transactionPort);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.dummyPasswordHash = passwordHasher.hash("mot-de-passe-factice-comparaison-a-temps-constant");
    }

    @Override
    public PatientSession register(RegisterPatientCommand command) {
        final PhoneNumber phone = PhoneNumber.of(command.phone());
        validatePassword(command.password());

        // Contrôle applicatif ; la contrainte d'unicité en base reste le dernier
        // rempart contre une course entre deux inscriptions simultanées.
        // Impasse latente si une désactivation existe un jour (aucune
        // aujourd'hui) : voir Javadoc de LoadPatientPort#existsByPhone.
        if (loadPatient.existsByPhone(phone)) {
            throw new PhoneAlreadyRegisteredException();
        }

        final PatientAccount account = PatientAccount.register(
                idGenerator.get(), phone, passwordHasher.hash(command.password()), clock.instant());

        // Frontière transactionnelle du cas d'usage (issue #130) : le compte et
        // sa première session sont committés ensemble, tout ou rien.
        return transactionPort.inTransaction(() -> {
            savePatient.save(account);
            final String token = sessions.issueToken(account.id());
            return new PatientSession(account.id(), token);
        });
    }

    @Override
    public PatientSession login(LoginCommand command) {
        final PhoneNumber phone;
        try {
            phone = PhoneNumber.of(command.phone());
        } catch (PatientValidationException invalid) {
            // Un téléphone mal formé ne révèle rien de plus qu'un identifiant erroné.
            throw new InvalidCredentialsException();
        }

        final Optional<PatientAccount> found = loadPatient.findByPhone(phone);
        // Comparaison BCrypt INCONDITIONNELLE (temps constant) : que le compte
        // existe ou non, actif ou non, le même travail cryptographique est
        // effectué avant de conclure. Message unique quel que soit le motif
        // (compte inconnu, inactif, mot de passe faux) : on ne divulgue pas
        // l'existence d'un compte, ni par le message ni par la latence.
        final String hashToCompare = found.map(PatientAccount::passwordHash).orElse(dummyPasswordHash);
        final boolean passwordMatches = passwordHasher.matches(command.password(), hashToCompare);
        if (found.isEmpty() || !found.get().active() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        final UUID patientId = found.get().id();
        return new PatientSession(patientId, sessions.issueToken(patientId));
    }

    @Override
    public void revoke(String rawToken) {
        sessions.revoke(rawToken);
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new PatientValidationException(
                    "Le mot de passe doit comporter au moins " + MIN_PASSWORD_LENGTH + " caractères");
        }
    }
}
