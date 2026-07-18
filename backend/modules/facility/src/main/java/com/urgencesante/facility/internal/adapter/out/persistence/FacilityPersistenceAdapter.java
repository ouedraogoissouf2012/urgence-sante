package com.urgencesante.facility.internal.adapter.out.persistence;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.facility.internal.adapter.out.persistence.entity.FacilityJpaEntity;
import com.urgencesante.facility.internal.adapter.out.persistence.mapper.FacilityEntityMapper;
import com.urgencesante.facility.internal.adapter.out.persistence.repository.FacilitySpringRepository;
import com.urgencesante.facility.internal.application.port.out.LoadFacilityPort;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de persistance : implémente le port sortant à partir du repository
 * Spring Data et du mapper. La recherche est déléguée à PostGIS (tri par
 * proximité), puis les entités sont chargées en préservant l'ordre.
 */
@Component
public class FacilityPersistenceAdapter implements LoadFacilityPort {

    private final FacilitySpringRepository repository;
    private final FacilityEntityMapper mapper;

    public FacilityPersistenceAdapter(FacilitySpringRepository repository, FacilityEntityMapper mapper) {
        this.repository = Objects.requireNonNull(repository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public Page<Facility> search(FindFacilitiesQuery query) {
        final boolean hasService = query.service().isPresent();
        final String service = query.service().map(MedicalServiceCode::value).orElse("");
        final boolean hasPoint = query.near().isPresent();
        final double lat = query.near().map(GeoLocation::latitude).orElse(0.0);
        final double lon = query.near().map(GeoLocation::longitude).orElse(0.0);
        final int radius = query.radiusMeters().orElse(1);

        final List<UUID> orderedIds = repository.searchIds(
                hasService, service, hasPoint, lat, lon, radius,
                query.page().size(), query.page().offset());
        final long total = repository.countSearch(hasService, service, hasPoint, lat, lon, radius);

        final Map<UUID, FacilityJpaEntity> byId = repository.findAllById(orderedIds).stream()
                .collect(Collectors.toMap(FacilityJpaEntity::getId, Function.identity()));
        final List<Facility> facilities = orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(mapper::toDomain)
                .toList();

        return new Page<>(facilities, query.page().page(), query.page().size(), total);
    }

    @Override
    public Optional<Facility> findById(FacilityId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }
}
