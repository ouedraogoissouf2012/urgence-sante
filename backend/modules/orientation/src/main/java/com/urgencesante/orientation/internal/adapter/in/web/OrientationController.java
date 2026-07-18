package com.urgencesante.orientation.internal.adapter.in.web;

import com.urgencesante.orientation.internal.adapter.in.web.dto.response.RecommendationResponse;
import com.urgencesante.orientation.internal.adapter.in.web.mapper.OrientationWebMapper;
import com.urgencesante.orientation.internal.application.port.in.RecommendFacilitiesUseCase;
import com.urgencesante.orientation.internal.application.query.OrientationQuery;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST de l'orientation. Conforme au contrat OpenAPI. Aucune
 * logique métier : validation, mapping et appel du port entrant.
 */
@RestController
@RequestMapping("/api/v1/orientation")
public class OrientationController {

    private static final int DEFAULT_RADIUS_METERS = 15_000;
    private static final int DEFAULT_LIMIT = 5;

    private final RecommendFacilitiesUseCase recommendFacilities;
    private final OrientationWebMapper mapper;

    public OrientationController(
            RecommendFacilitiesUseCase recommendFacilities, OrientationWebMapper mapper) {
        this.recommendFacilities = recommendFacilities;
        this.mapper = mapper;
    }

    @GetMapping
    public List<RecommendationResponse> recommend(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String service,
            @RequestParam(required = false) Integer radiusMeters,
            @RequestParam(required = false) Integer limit) {
        final OrientationQuery query = new OrientationQuery(
                lat, lon, service,
                radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters,
                limit == null ? DEFAULT_LIMIT : limit);
        return recommendFacilities.recommend(query).stream().map(mapper::toResponse).toList();
    }
}
