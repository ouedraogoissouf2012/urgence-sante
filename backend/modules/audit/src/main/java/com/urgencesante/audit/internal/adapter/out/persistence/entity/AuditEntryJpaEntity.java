package com.urgencesante.audit.internal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Ligne d'audit persistée. L'identifiant est celui de l'événement source. */
@Entity
@Table(name = "audit_entry")
public class AuditEntryJpaEntity {

    @Id
    @Column(name = "source_event_id")
    private UUID sourceEventId;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "actor_label")
    private String actorLabel;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEntryJpaEntity() {
        // requis par JPA
    }

    public AuditEntryJpaEntity(
            UUID sourceEventId,
            String action,
            String resourceType,
            String resourceId,
            String actorId,
            String actorLabel,
            String correlationId,
            Instant occurredAt) {
        this.sourceEventId = sourceEventId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.actorId = actorId;
        this.actorLabel = actorLabel;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getActorLabel() {
        return actorLabel;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
