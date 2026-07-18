package com.urgencesante.availability.internal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Disponibilité courante (remplacée à chaque mise à jour). */
@Entity
@Table(name = "availability")
public class AvailabilityJpaEntity {

    @Id
    private UUID id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(nullable = false)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AvailabilityJpaEntity() {
        // requis par JPA
    }

    public AvailabilityJpaEntity(UUID facilityId, String serviceCode, String status, Instant updatedAt) {
        this.id = UUID.randomUUID();
        this.facilityId = facilityId;
        this.serviceCode = serviceCode;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    /** Applique une nouvelle valeur de statut horodatée. */
    public void applyUpdate(String status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public UUID getFacilityId() {
        return facilityId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
