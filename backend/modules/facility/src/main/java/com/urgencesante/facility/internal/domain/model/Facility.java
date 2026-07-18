package com.urgencesante.facility.internal.domain.model;

import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Établissement de santé. Agrégat du domaine, sans dépendance framework.
 *
 * <p>L'identité repose sur {@link FacilityId} ; le reste des attributs est
 * validé à la construction (nom non vide, localisation et services requis).
 */
public final class Facility {

    private final FacilityId id;
    private final String name;
    private final GeoLocation location;
    private final String phone;
    private final Set<MedicalServiceCode> services;

    private Facility(
            FacilityId id,
            String name,
            GeoLocation location,
            String phone,
            Set<MedicalServiceCode> services) {
        this.id = Objects.requireNonNull(id, "L'identifiant est requis");
        this.location = Objects.requireNonNull(location, "La localisation est requise");
        this.name = requireName(name);
        this.phone = normalizePhone(phone);
        this.services = Collections.unmodifiableSet(
                new LinkedHashSet<>(Objects.requireNonNull(services, "Les services sont requis")));
    }

    public static Facility of(
            FacilityId id,
            String name,
            GeoLocation location,
            String phone,
            Set<MedicalServiceCode> services) {
        return new Facility(id, name, location, phone, services);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new FacilityValidationException("Le nom de l'établissement est requis");
        }
        return name.trim();
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        final String trimmed = phone.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Indique si l'établissement offre le service demandé. */
    public boolean offers(MedicalServiceCode service) {
        return services.contains(service);
    }

    public FacilityId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public GeoLocation location() {
        return location;
    }

    public Optional<String> phone() {
        return Optional.ofNullable(phone);
    }

    public Set<MedicalServiceCode> services() {
        return services;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Facility facility && id.equals(facility.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
