package com.urgencesante.patient.internal.adapter.out.persistence.entity;

import com.urgencesante.patient.internal.domain.model.PatientAccount;
import com.urgencesante.patient.internal.domain.model.PhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Compte patient persisté. Le mot de passe n'est stocké que sous forme d'empreinte BCrypt. */
@Entity
@Table(name = "patient_account")
public class PatientAccountJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PatientAccountJpaEntity() {
        // requis par JPA
    }

    public static PatientAccountJpaEntity fromDomain(PatientAccount account) {
        final PatientAccountJpaEntity entity = new PatientAccountJpaEntity();
        entity.id = account.id();
        entity.phone = account.phone().value();
        entity.passwordHash = account.passwordHash();
        entity.active = account.active();
        entity.createdAt = account.createdAt();
        return entity;
    }

    public PatientAccount toDomain() {
        return PatientAccount.restore(id, new PhoneNumber(phone), passwordHash, active, createdAt);
    }
}
