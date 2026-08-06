package com.urgencesante.emergencytaxonomy.internal.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Catégorie d'urgence exposée par l'API (conforme au schéma OpenAPI
 * EmergencyCategory). {@code directCallMessage} n'est sérialisé que pour les
 * catégories à appel direct (omis — non nul — sinon).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmergencyCategoryResponse(
        String id,
        String label,
        boolean directCallOnly,
        String directCallMessage,
        List<SymptomResponse> symptoms,
        List<String> serviceCodes) {
}
