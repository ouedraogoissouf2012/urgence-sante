package com.urgencesante.availability.internal.adapter.out.persistence.repository;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.OutboxJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository de l'outbox (détail de persistance). */
public interface OutboxSpringRepository extends JpaRepository<OutboxJpaEntity, UUID> {

    List<OutboxJpaEntity> findByPublishedAtIsNullOrderByOccurredAtAsc(Pageable pageable);
}
