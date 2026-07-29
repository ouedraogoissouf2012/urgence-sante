package com.urgencesante.patient.internal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Session patient : empreinte du jeton porteur (jamais le jeton en clair) +
 * date d'expiration. Un compte peut avoir plusieurs sessions (appareils).
 */
@Entity
@Table(name = "patient_session")
public class PatientSessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected PatientSessionJpaEntity() {
        // requis par JPA
    }

    public static PatientSessionJpaEntity of(
            UUID id, UUID patientId, String tokenHash, Instant createdAt, Instant expiresAt) {
        final PatientSessionJpaEntity entity = new PatientSessionJpaEntity();
        entity.id = id;
        entity.patientId = patientId;
        entity.tokenHash = tokenHash;
        entity.createdAt = createdAt;
        entity.expiresAt = expiresAt;
        return entity;
    }

    public UUID patientId() {
        return patientId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
