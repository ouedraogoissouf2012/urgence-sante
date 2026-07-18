package com.urgencesante.availability.internal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Trace historique d'une mise à jour de disponibilité (insertion seule). */
@Entity
@Table(name = "availability_history")
public class AvailabilityHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(nullable = false)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AvailabilityHistoryJpaEntity() {
        // requis par JPA
    }

    public AvailabilityHistoryJpaEntity(UUID facilityId, String serviceCode, String status, Instant updatedAt) {
        this.facilityId = facilityId;
        this.serviceCode = serviceCode;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
