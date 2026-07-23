package com.urgencesante.facility.internal.adapter.out.persistence.repository;

import com.urgencesante.facility.internal.adapter.out.persistence.entity.FacilityJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

/**
 * Repository Spring Data (détail de persistance). Manipule uniquement les
 * entités JPA. La recherche géographique s'appuie sur PostGIS.
 *
 * <p>Les booléens {@code hasService}/{@code hasPoint} évitent tout paramètre lié
 * à {@code NULL} dans les requêtes natives : les branches géographiques ne sont
 * évaluées que lorsqu'elles sont demandées.
 */
public interface FacilitySpringRepository extends JpaRepository<FacilityJpaEntity, UUID> {

    // Chargements par identifiant AVEC les services en un seul JOIN (@EntityGraph) :
    // la collection `services` est LAZY (évite le N+1 en liste), ces méthodes
    // garantissent qu'elle est hydratée sans lazy-init hors transaction.
    @Override
    @EntityGraph(attributePaths = "services")
    @NonNull
    Optional<FacilityJpaEntity> findById(@NonNull UUID id);

    @Override
    @EntityGraph(attributePaths = "services")
    @NonNull
    List<FacilityJpaEntity> findAllById(@NonNull Iterable<UUID> ids);

    @Query(value = """
            SELECT f.id FROM facility f
            WHERE (NOT :hasService OR EXISTS (
                     SELECT 1 FROM facility_service fs
                     WHERE fs.facility_id = f.id AND fs.service_code = :service))
              AND (NOT :hasPoint OR ST_DWithin(
                     f.location,
                     ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                     :radius))
            ORDER BY
              (CASE WHEN :hasPoint THEN ST_Distance(
                     f.location,
                     ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)
                    ELSE NULL END) NULLS LAST,
              f.name
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UUID> searchIds(
            @Param("hasService") boolean hasService,
            @Param("service") String service,
            @Param("hasPoint") boolean hasPoint,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") int radius,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query(value = """
            SELECT count(*) FROM facility f
            WHERE (NOT :hasService OR EXISTS (
                     SELECT 1 FROM facility_service fs
                     WHERE fs.facility_id = f.id AND fs.service_code = :service))
              AND (NOT :hasPoint OR ST_DWithin(
                     f.location,
                     ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                     :radius))
            """, nativeQuery = true)
    long countSearch(
            @Param("hasService") boolean hasService,
            @Param("service") String service,
            @Param("hasPoint") boolean hasPoint,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") int radius);
}
