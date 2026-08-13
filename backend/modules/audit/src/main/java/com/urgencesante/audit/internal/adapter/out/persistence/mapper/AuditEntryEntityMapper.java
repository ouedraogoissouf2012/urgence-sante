package com.urgencesante.audit.internal.adapter.out.persistence.mapper;

import com.urgencesante.audit.internal.adapter.out.persistence.entity.AuditEntryJpaEntity;
import com.urgencesante.audit.internal.domain.model.AuditEntry;
import org.springframework.stereotype.Component;

/** Traduit entre {@link AuditEntry} (domaine) et {@link AuditEntryJpaEntity} (persistance). */
@Component
public class AuditEntryEntityMapper {

    public AuditEntryJpaEntity toEntity(AuditEntry entry) {
        return new AuditEntryJpaEntity(
                entry.id(),
                entry.action(),
                entry.resourceType(),
                entry.resourceId(),
                entry.actorId().orElse(null),
                entry.actorLabel().orElse(null),
                entry.correlationId(),
                entry.occurredAt());
    }
}
