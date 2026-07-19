package com.urgencesante.identity.internal.adapter.out.persistence.entity;

import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.domain.model.PortalCredential;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Identifiant du portail persisté (empreinte de jeton, jamais le jeton). */
@Entity
@Table(name = "portal_credential")
public class PortalCredentialJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String label;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PortalRole role;

    @Column(name = "facility_id")
    private UUID facilityId;

    @Column(nullable = false)
    private boolean active;

    protected PortalCredentialJpaEntity() {
        // requis par JPA
    }

    public PortalCredential toDomain() {
        return new PortalCredential(id, label, tokenHash, role, facilityId, active);
    }
}
