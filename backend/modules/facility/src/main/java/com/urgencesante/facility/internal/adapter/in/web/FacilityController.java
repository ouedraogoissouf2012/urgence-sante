package com.urgencesante.facility.internal.adapter.in.web;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.buildingblocks.pagination.PageRequest;
import com.urgencesante.facility.internal.adapter.in.web.dto.response.FacilityResponse;
import com.urgencesante.facility.internal.adapter.in.web.dto.response.PagedFacilitiesResponse;
import com.urgencesante.facility.internal.adapter.in.web.mapper.FacilityWebMapper;
import com.urgencesante.facility.internal.application.port.in.FindFacilitiesUseCase;
import com.urgencesante.facility.internal.application.port.in.GetFacilityUseCase;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST : traduit les requêtes HTTP en cas d'usage, sans
 * logique métier. Conforme au contrat OpenAPI (docs/api/openapi.yaml).
 */
@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {

    private static final int DEFAULT_RADIUS_METERS = 5000;

    private final FindFacilitiesUseCase findFacilities;
    private final GetFacilityUseCase getFacility;
    private final FacilityWebMapper mapper;

    public FacilityController(
            FindFacilitiesUseCase findFacilities,
            GetFacilityUseCase getFacility,
            FacilityWebMapper mapper) {
        this.findFacilities = findFacilities;
        this.getFacility = getFacility;
        this.mapper = mapper;
    }

    @GetMapping
    public PagedFacilitiesResponse list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Integer radiusMeters) {

        final Optional<GeoLocation> near = toSearchPoint(lat, lon);
        final Optional<Integer> radius = near.isPresent()
                ? Optional.of(radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters)
                : Optional.empty();

        final FindFacilitiesQuery query = new FindFacilitiesQuery(
                Optional.ofNullable(service).map(MedicalServiceCode::of),
                near,
                radius,
                PageRequest.of(page, size));

        final Page<Facility> result = findFacilities.findFacilities(query);
        return mapper.toPagedResponse(result, near.orElse(null));
    }

    @GetMapping("/{facilityId}")
    public FacilityResponse get(@PathVariable String facilityId) {
        final Facility facility = getFacility.getFacility(FacilityId.fromString(facilityId));
        return mapper.toResponse(facility, null);
    }

    private static Optional<GeoLocation> toSearchPoint(Double lat, Double lon) {
        if (lat == null && lon == null) {
            return Optional.empty();
        }
        if (lat == null || lon == null) {
            throw new FacilityValidationException("Les paramètres « lat » et « lon » vont de pair");
        }
        return Optional.of(new GeoLocation(lat, lon));
    }
}
