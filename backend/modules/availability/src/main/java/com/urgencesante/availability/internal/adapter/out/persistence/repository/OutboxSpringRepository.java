package com.urgencesante.availability.internal.adapter.out.persistence.repository;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.OutboxJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository de l'outbox (détail de persistance). */
public interface OutboxSpringRepository extends JpaRepository<OutboxJpaEntity, UUID> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_READ)
    @Query("SELECT o FROM OutboxJpaEntity o WHERE o.publishedAt IS NULL ORDER BY o.occurredAt ASC")
    List<OutboxJpaEntity> findByPublishedAtIsNullOrderByOccurredAtAscWithLock(Pageable pageable);

    List<OutboxJpaEntity> findByPublishedAtIsNullOrderByOccurredAtAsc(Pageable pageable);
}
