package com.urgencesante.facility.internal.domain.directory;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Enregistrement d'import d'un établissement, avec traçabilité.
 *
 * @param source provenance (jeu de données, autorité)
 * @param externalRef référence stable dans la source (clé d'idempotence)
 * @param verifiedAt date de vérification (obligatoire si {@code VERIFIED})
 * @param steward responsable de la donnée
 */
public record FacilityImportRecord(
        String source,
        String externalRef,
        String name,
        String phone,
        double latitude,
        double longitude,
        Set<String> services,
        DataStatus dataStatus,
        LocalDate verifiedAt,
        String steward) {

    public FacilityImportRecord {
        services = services == null ? Set.of() : Set.copyOf(services);
    }

    public List<String> serviceList() {
        return List.copyOf(services);
    }
}
