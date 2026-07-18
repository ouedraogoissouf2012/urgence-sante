package com.urgencesante.medicalservice.internal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Représentation de persistance d'un service du catalogue. */
@Entity
@Table(name = "medical_service")
public class MedicalServiceJpaEntity {

    @Id
    private String code;

    @Column(nullable = false)
    private String label;

    private String category;

    protected MedicalServiceJpaEntity() {
        // requis par JPA
    }

    public MedicalServiceJpaEntity(String code, String label, String category) {
        this.code = code;
        this.label = label;
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }
}
