package com.urgencesante.facility.internal.adapter.in.web.mapper;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.facility.internal.adapter.in.web.dto.response.FacilityResponse;
import com.urgencesante.facility.internal.adapter.in.web.dto.response.GeoPointResponse;
import com.urgencesante.facility.internal.adapter.in.web.dto.response.PageMetadataResponse;
import com.urgencesante.facility.internal.adapter.in.web.dto.response.PagedFacilitiesResponse;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import org.springframework.stereotype.Component;

/** Traduit le domaine en DTO de réponse HTTP, sans logique métier. */
@Component
public class FacilityWebMapper {

    /**
     * @param searchPoint point de recherche pour calculer la distance affichée,
     *     ou {@code null} pour une consultation sans proximité
     */
    public FacilityResponse toResponse(Facility facility, GeoLocation searchPoint) {
        final Integer distanceMeters = searchPoint == null
                ? null
                : (int) Math.round(facility.location().distanceMetersTo(searchPoint));
        return new FacilityResponse(
                facility.id().value().toString(),
                facility.name(),
                new GeoPointResponse(facility.location().latitude(), facility.location().longitude()),
                facility.phone().orElse(null),
                facility.services().stream().map(MedicalServiceCode::value).sorted().toList(),
                distanceMeters);
    }

    public PagedFacilitiesResponse toPagedResponse(Page<Facility> page, GeoLocation searchPoint) {
        return new PagedFacilitiesResponse(
                page.content().stream().map(facility -> toResponse(facility, searchPoint)).toList(),
                new PageMetadataResponse(
                        page.number(), page.size(), page.totalElements(), page.totalPages()));
    }
}
