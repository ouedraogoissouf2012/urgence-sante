package com.urgencesante.audit.internal.domain.model;

import com.urgencesante.audit.internal.domain.exception.AuditValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Ligne de la trace d'audit : un fait métier ou de sécurité déjà survenu,
 * jamais modifié après écriture. Agrégat du domaine, sans dépendance
 * framework.
 *
 * <p>L'identité ({@code id}) est celle de l'événement source qui a produit
 * cette ligne (ex. {@code AvailabilityUpdated.eventId()}) — pas un
 * identifiant généré séparément. Une ligne d'audit correspond exactement à un
 * événement source ; c'est ce qui rend la persistance idempotente face à une
 * livraison au moins une fois (voir {@code OutboxRelay}).
 *
 * <p>{@code actorId}/{@code actorLabel} sont volontairement nullable : pour
 * l'événement actuellement consommé ({@code AvailabilityUpdated}), l'acteur
 * est résolu par {@code PortalSecurityInterceptor} (bootstrap) mais n'est
 * propagé ni dans la commande ni dans l'événement — dette connue, hors
 * périmètre de ce module (voir auto-critique de la PR).
 */
public final class AuditEntry {

    private final UUID id;
    private final String action;
    private final String resourceType;
    private final String resourceId;
    private final String actorId;
    private final String actorLabel;
    private final String correlationId;
    private final Instant occurredAt;

    private AuditEntry(
            UUID id,
            String action,
            String resourceType,
            String resourceId,
            String actorId,
            String actorLabel,
            String correlationId,
            Instant occurredAt) {
        this.id = Objects.requireNonNull(id, "L'identifiant est requis");
        this.action = requireNonBlank(action, "L'action est requise");
        this.resourceType = requireNonBlank(resourceType, "Le type de ressource est requis");
        this.resourceId = requireNonBlank(resourceId, "L'identifiant de ressource est requis");
        this.actorId = normalize(actorId);
        this.actorLabel = normalize(actorLabel);
        this.correlationId = requireNonBlank(correlationId, "L'identifiant de corrélation est requis");
        this.occurredAt = Objects.requireNonNull(occurredAt, "La date du fait est requise");
    }

    public static AuditEntry of(
            UUID id,
            String action,
            String resourceType,
            String resourceId,
            String actorId,
            String actorLabel,
            String correlationId,
            Instant occurredAt) {
        return new AuditEntry(
                id, action, resourceType, resourceId, actorId, actorLabel, correlationId, occurredAt);
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new AuditValidationException(message);
        }
        return value.trim();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID id() {
        return id;
    }

    public String action() {
        return action;
    }

    public String resourceType() {
        return resourceType;
    }

    public String resourceId() {
        return resourceId;
    }

    public Optional<String> actorId() {
        return Optional.ofNullable(actorId);
    }

    public Optional<String> actorLabel() {
        return Optional.ofNullable(actorLabel);
    }

    public String correlationId() {
        return correlationId;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof AuditEntry entry && id.equals(entry.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
