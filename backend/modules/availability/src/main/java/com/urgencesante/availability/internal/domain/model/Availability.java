package com.urgencesante.availability.internal.domain.model;

import com.urgencesante.availability.internal.domain.exception.AvailabilityValidationException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Disponibilité courante d'un service dans un établissement. Agrégat du domaine.
 * L'identité repose sur le couple (établissement, service).
 */
public final class Availability {

    private final UUID facilityId;
    private final String serviceCode;
    private final AvailabilityStatus status;
    private final Instant updatedAt;

    private Availability(UUID facilityId, String serviceCode, AvailabilityStatus status, Instant updatedAt) {
        this.facilityId = Objects.requireNonNull(facilityId, "L'établissement est requis");
        this.status = Objects.requireNonNull(status, "Le statut est requis");
        this.updatedAt = Objects.requireNonNull(updatedAt, "L'horodatage est requis");
        this.serviceCode = requireServiceCode(serviceCode);
    }

    public static Availability of(
            UUID facilityId, String serviceCode, AvailabilityStatus status, Instant updatedAt) {
        return new Availability(facilityId, serviceCode, status, updatedAt);
    }

    private static String requireServiceCode(String serviceCode) {
        if (serviceCode == null || serviceCode.isBlank()) {
            throw new AvailabilityValidationException("Le code de service est requis");
        }
        return serviceCode.trim().toLowerCase(Locale.ROOT);
    }

    public UUID facilityId() {
        return facilityId;
    }

    public String serviceCode() {
        return serviceCode;
    }

    public AvailabilityStatus status() {
        return status;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Availability that
                && facilityId.equals(that.facilityId)
                && serviceCode.equals(that.serviceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(facilityId, serviceCode);
    }
}
