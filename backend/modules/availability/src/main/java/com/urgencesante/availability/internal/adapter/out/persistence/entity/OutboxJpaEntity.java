package com.urgencesante.availability.internal.adapter.out.persistence.entity;

import com.urgencesante.availability.AvailabilityUpdated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Ligne d'outbox : événement de disponibilité en attente de publication. */
@Entity
@Table(name = "availability_outbox")
public class OutboxJpaEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(nullable = false)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    private int attempts;

    protected OutboxJpaEntity() {
        // requis par JPA
    }

    public static OutboxJpaEntity from(AvailabilityUpdated event) {
        final OutboxJpaEntity entity = new OutboxJpaEntity();
        entity.eventId = event.eventId();
        entity.facilityId = event.facilityId();
        entity.serviceCode = event.serviceCode();
        entity.status = event.status();
        entity.updatedAt = event.updatedAt();
        entity.correlationId = event.correlationId();
        entity.occurredAt = event.occurredAt();
        entity.attempts = 0;
        return entity;
    }

    public AvailabilityUpdated toEvent() {
        return new AvailabilityUpdated(
                eventId, correlationId, facilityId, serviceCode, status, updatedAt, occurredAt);
    }

    public void markPublished(Instant when) {
        this.publishedAt = when;
    }

    public void recordFailure() {
        this.attempts++;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttempts() {
        return attempts;
    }
}
