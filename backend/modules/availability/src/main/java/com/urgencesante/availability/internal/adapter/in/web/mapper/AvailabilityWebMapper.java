package com.urgencesante.availability.internal.adapter.in.web.mapper;

import com.urgencesante.availability.internal.adapter.in.web.dto.response.FacilityAvailabilityResponse;
import com.urgencesante.availability.internal.adapter.in.web.dto.response.ServiceAvailabilityResponse;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import org.springframework.stereotype.Component;

/** Traduit les résultats applicatifs en DTO de réponse HTTP. */
@Component
public class AvailabilityWebMapper {

    public ServiceAvailabilityResponse toResponse(ServiceAvailabilitySnapshot snapshot) {
        return new ServiceAvailabilityResponse(
                snapshot.serviceCode(),
                snapshot.status().name(),
                snapshot.freshness().name(),
                snapshot.updatedAt());
    }

    public FacilityAvailabilityResponse toResponse(FacilityAvailabilitySnapshot snapshot) {
        return new FacilityAvailabilityResponse(
                snapshot.facilityId().toString(),
                snapshot.services().stream().map(this::toResponse).toList());
    }
}
