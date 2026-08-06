package com.urgencesante.emergencytaxonomy;

import java.util.List;
import java.util.Optional;

/**
 * Vue publique d'une catégorie d'urgence, exposée aux autres modules. Ne révèle
 * aucun type interne.
 */
public record EmergencyCategoryView(
        String id,
        String label,
        boolean directCallOnly,
        Optional<String> directCallMessage,
        List<SymptomView> symptoms,
        List<String> serviceCodes) {
}
