package com.urgencesante.facility.internal.adapter.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.Point;

/**
 * Représentation de persistance d'un établissement. Détail de l'adaptateur : ne
 * sort jamais du package de persistance (mappée vers le domaine par un mapper).
 */
@Entity
@Table(name = "facility")
public class FacilityJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    // LAZY (et non EAGER) pour éviter un N+1 en lecture paginée : avec EAGER,
    // charger N établissements déclenchait N requêtes supplémentaires (une par
    // collection de services). Le chargement est garanti sans lazy-init par un
    // @EntityGraph("services") sur les méthodes de lecture du repository, qui
    // ramène les services en UN SEUL JOIN.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "facility_service",
            joinColumns = @JoinColumn(name = "facility_id"))
    @Column(name = "service_code", nullable = false)
    private Set<String> services = new LinkedHashSet<>();

    protected FacilityJpaEntity() {
        // requis par JPA
    }

    public FacilityJpaEntity(UUID id, String name, String phone, Point location, Set<String> services) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.location = location;
        this.services = new LinkedHashSet<>(services);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Point getLocation() {
        return location;
    }

    public Set<String> getServices() {
        return services;
    }
}
