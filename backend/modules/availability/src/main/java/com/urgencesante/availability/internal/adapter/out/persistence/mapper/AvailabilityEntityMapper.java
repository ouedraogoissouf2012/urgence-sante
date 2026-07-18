package com.urgencesante.availability.internal.adapter.out.persistence.mapper;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.AvailabilityJpaEntity;
import com.urgencesante.availability.internal.domain.model.Availability;
import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import org.springframework.stereotype.Component;

/** Traduit l'entité de persistance en agrégat du domaine. */
@Component
public class AvailabilityEntityMapper {

    public Availability toDomain(AvailabilityJpaEntity entity) {
        return Availability.of(
                entity.getFacilityId(),
                entity.getServiceCode(),
                AvailabilityStatus.valueOf(entity.getStatus()),
                entity.getUpdatedAt());
    }
}
