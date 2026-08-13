package com.urgencesante.audit.internal.adapter.out.persistence.repository;

import com.urgencesante.audit.internal.adapter.out.persistence.entity.AuditEntryJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntrySpringRepository extends JpaRepository<AuditEntryJpaEntity, UUID> {
}
