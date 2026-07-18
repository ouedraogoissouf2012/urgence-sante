package com.urgencesante.availability.internal.adapter.out.persistence.repository;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.AvailabilityHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository de l'historique (insertion seule). */
public interface AvailabilityHistorySpringRepository
        extends JpaRepository<AvailabilityHistoryJpaEntity, Long> {
}
