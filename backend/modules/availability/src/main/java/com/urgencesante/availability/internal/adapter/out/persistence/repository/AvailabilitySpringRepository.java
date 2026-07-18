package com.urgencesante.availability.internal.adapter.out.persistence.repository;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.AvailabilityJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository de la disponibilité courante (détail de persistance). */
public interface AvailabilitySpringRepository extends JpaRepository<AvailabilityJpaEntity, UUID> {

    Optional<AvailabilityJpaEntity> findByFacilityIdAndServiceCode(UUID facilityId, String serviceCode);

    List<AvailabilityJpaEntity> findByFacilityIdOrderByServiceCodeAsc(UUID facilityId);
}
