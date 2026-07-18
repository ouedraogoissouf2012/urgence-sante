package com.urgencesante.facility.internal.adapter.in.web.dto.response;

/** Métadonnées de pagination exposées (conforme au schéma OpenAPI PageMetadata). */
public record PageMetadataResponse(int number, int size, long totalElements, int totalPages) {
}
