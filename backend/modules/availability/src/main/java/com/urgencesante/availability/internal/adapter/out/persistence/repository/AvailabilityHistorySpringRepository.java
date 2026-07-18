package com.urgencesante.availability.internal.adapter.out.persistence.repository;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.AvailabilityHistoryJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository de l'historique (insertion + lecture chronologique). */
public interface AvailabilityHistorySpringRepository
        extends JpaRepository<AvailabilityHistoryJpaEntity, Long> {

    List<AvailabilityHistoryJpaEntity> findByFacilityIdAndServiceCodeOrderByUpdatedAtDesc(
            UUID facilityId, String serviceCode, Pageable pageable);
}
