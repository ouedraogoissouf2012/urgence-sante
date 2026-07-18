package com.urgencesante.facility.internal.application.query;

import com.urgencesante.buildingblocks.pagination.PageRequest;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import java.util.Objects;
import java.util.Optional;

/**
 * Critères de recherche d'établissements.
 *
 * <p>Filtre optionnel par service médical et tri optionnel par proximité (une
 * position implique un rayon). La pagination est toujours requise.
 */
public record FindFacilitiesQuery(
        Optional<MedicalServiceCode> service,
        Optional<GeoLocation> near,
        Optional<Integer> radiusMeters,
        PageRequest page) {

    public FindFacilitiesQuery {
        service = service == null ? Optional.empty() : service;
        near = near == null ? Optional.empty() : near;
        radiusMeters = radiusMeters == null ? Optional.empty() : radiusMeters;
        Objects.requireNonNull(page, "La pagination est requise");
        if (near.isPresent() && radiusMeters.isEmpty()) {
            throw new IllegalArgumentException("Un rayon est requis lorsqu'une position est fournie");
        }
        if (radiusMeters.isPresent() && radiusMeters.get() < 1) {
            throw new IllegalArgumentException("Le rayon doit être >= 1 mètre");
        }
    }

    /** Vrai si la recherche doit être triée par proximité. */
    public boolean isProximitySearch() {
        return near.isPresent();
    }
}
