package com.urgencesante.patient.internal.application.service;

import com.urgencesante.patient.internal.application.command.LoginCommand;
import com.urgencesante.patient.internal.application.command.RegisterPatientCommand;
import com.urgencesante.patient.internal.application.port.in.AuthenticatePatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RegisterPatientUseCase;
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
public class PatientService implements RegisterPatientUseCase, AuthenticatePatientUseCase {

    /** Longueur minimale d'un mot de passe (borne serveur, non négociable). */
    static final int MIN_PASSWORD_LENGTH = 8;

    private final LoadPatientPort loadPatient;
    private final SavePatientPort savePatient;
    private final PasswordHasherPort passwordHasher;
    private final PatientSessionPort sessions;
    private final Clock clock;
    private final Supplier<UUID> idGenerator;

    public PatientService(
            LoadPatientPort loadPatient,
            SavePatientPort savePatient,
            PasswordHasherPort passwordHasher,
            PatientSessionPort sessions,
            Clock clock,
            Supplier<UUID> idGenerator) {
        this.loadPatient = Objects.requireNonNull(loadPatient);
        this.savePatient = Objects.requireNonNull(savePatient);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.sessions = Objects.requireNonNull(sessions);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public UUID register(RegisterPatientCommand command) {
        final PhoneNumber phone = PhoneNumber.of(command.phone());
        validatePassword(command.password());

        // Contrôle applicatif ; la contrainte d'unicité en base reste le dernier
        // rempart contre une course entre deux inscriptions simultanées.
        if (loadPatient.existsByPhone(phone)) {
            throw new PhoneAlreadyRegisteredException();
        }

        final PatientAccount account = PatientAccount.register(
                idGenerator.get(), phone, passwordHasher.hash(command.password()), clock.instant());
        savePatient.save(account);
        return account.id();
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
        // Message unique quel que soit le motif (compte inconnu, inactif, mot de
        // passe faux) : on ne divulgue pas l'existence d'un compte.
        if (found.isEmpty()
                || !found.get().active()
                || !passwordHasher.matches(command.password(), found.get().passwordHash())) {
            throw new InvalidCredentialsException();
        }

        final UUID patientId = found.get().id();
        return new PatientSession(patientId, sessions.issueToken(patientId));
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new PatientValidationException(
                    "Le mot de passe doit comporter au moins " + MIN_PASSWORD_LENGTH + " caractères");
        }
    }
}
