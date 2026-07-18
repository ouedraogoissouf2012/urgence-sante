package com.urgencesante.facility;

import java.util.Set;
import java.util.UUID;

/**
 * Vue publique d'un établissement, exposée aux autres modules. Ne révèle aucun
 * type interne du module.
 */
public record FacilityView(
        UUID id,
        String name,
        double latitude,
        double longitude,
        Set<String> services) {
}
