package com.urgencesante.orientation.internal.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Port sortant : statuts de disponibilité des services d'un ensemble d'établissements. */
public interface AvailabilityLookupPort {

    /**
     * Statuts des services demandés, pour TOUS les établissements donnés, en UN
     * SEUL accès (issue #127 : jusqu'à 30 accès distincts auparavant, un par
     * candidat évalué). Un établissement absent de la map résultante n'a aucun
     * statut connu pour les services demandés ; un établissement présent liste
     * uniquement les services trouvés (silencieusement absents sinon).
     */
    Map<UUID, List<ServiceStatus>> statusesForFacilities(Set<UUID> facilityIds, Set<String> serviceCodes);

    /** Statut et fraîcheur bruts, tels qu'exposés par le module availability. */
    record ServiceStatus(String status, String freshness) {
    }
}
