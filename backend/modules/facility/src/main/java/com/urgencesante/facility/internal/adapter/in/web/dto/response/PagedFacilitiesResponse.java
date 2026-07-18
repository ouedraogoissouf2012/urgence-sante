package com.urgencesante.facility.internal.adapter.in.web.dto.response;

import java.util.List;

/** Page d'établissements exposée (conforme au schéma OpenAPI PagedFacilities). */
public record PagedFacilitiesResponse(List<FacilityResponse> content, PageMetadataResponse page) {
}
