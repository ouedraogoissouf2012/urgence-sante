package com.urgencesante.patient.internal.domain.model;

import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Compte patient : identité (téléphone) + empreinte du mot de passe.
 *
 * <p>Le domaine ne connaît JAMAIS le mot de passe en clair : il reçoit déjà son
 * empreinte (le hachage est une préoccupation applicative, port dédié). Aucune
 * donnée médicale ici — elles restent sur l'appareil.
 *
 * <p>Identité (equals/hashCode) portée par l'{@code id} seul : deux vues du même
 * compte sont le même compte, quels que soient les autres attributs.
 */
public final class PatientAccount {

    private final UUID id;
    private final PhoneNumber phone;
    private final String passwordHash;
    private final boolean active;
    private final Instant createdAt;

    private PatientAccount(
            UUID id, PhoneNumber phone, String passwordHash, boolean active, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id requis");
        this.phone = Objects.requireNonNull(phone, "téléphone requis");
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new PatientValidationException("Empreinte de mot de passe requise");
        }
        this.passwordHash = passwordHash;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "date de création requise");
    }

    /** Nouveau compte actif à l'inscription. */
    public static PatientAccount register(
            UUID id, PhoneNumber phone, String passwordHash, Instant createdAt) {
        return new PatientAccount(id, phone, passwordHash, true, createdAt);
    }

    /** Reconstitue un compte depuis la persistance (tous les attributs connus). */
    public static PatientAccount restore(
            UUID id, PhoneNumber phone, String passwordHash, boolean active, Instant createdAt) {
        return new PatientAccount(id, phone, passwordHash, active, createdAt);
    }

    public UUID id() {
        return id;
    }

    public PhoneNumber phone() {
        return phone;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PatientAccount account && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
