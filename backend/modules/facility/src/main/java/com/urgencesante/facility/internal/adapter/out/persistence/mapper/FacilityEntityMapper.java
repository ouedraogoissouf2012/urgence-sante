package com.urgencesante.facility.internal.adapter.out.persistence.mapper;

import com.urgencesante.facility.internal.adapter.out.persistence.entity.FacilityJpaEntity;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Traduit l'entité de persistance en agrégat du domaine. */
@Component
public class FacilityEntityMapper {

    public Facility toDomain(FacilityJpaEntity entity) {
        // JTS : X = longitude, Y = latitude.
        final GeoLocation location =
                new GeoLocation(entity.getLocation().getY(), entity.getLocation().getX());
        return Facility.of(
                FacilityId.of(entity.getId()),
                entity.getName(),
                location,
                entity.getPhone(),
                entity.getServices().stream()
                        .map(MedicalServiceCode::of)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}
