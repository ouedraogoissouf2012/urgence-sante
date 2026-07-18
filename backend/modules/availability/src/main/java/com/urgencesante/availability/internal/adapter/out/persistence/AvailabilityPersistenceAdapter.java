package com.urgencesante.availability.internal.adapter.out.persistence;

import com.urgencesante.availability.internal.adapter.out.persistence.entity.AvailabilityHistoryJpaEntity;
import com.urgencesante.availability.internal.adapter.out.persistence.entity.AvailabilityJpaEntity;
import com.urgencesante.availability.internal.adapter.out.persistence.mapper.AvailabilityEntityMapper;
import com.urgencesante.availability.internal.adapter.out.persistence.repository.AvailabilityHistorySpringRepository;
import com.urgencesante.availability.internal.adapter.out.persistence.repository.AvailabilitySpringRepository;
import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.domain.model.Availability;
import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptateur de persistance : remplace la disponibilité courante et ajoute une
 * ligne d'historique, dans la même transaction.
 */
@Component
public class AvailabilityPersistenceAdapter implements SaveAvailabilityPort, LoadAvailabilityPort {

    private final AvailabilitySpringRepository currentRepository;
    private final AvailabilityHistorySpringRepository historyRepository;
    private final AvailabilityEntityMapper mapper;

    public AvailabilityPersistenceAdapter(
            AvailabilitySpringRepository currentRepository,
            AvailabilityHistorySpringRepository historyRepository,
            AvailabilityEntityMapper mapper) {
        this.currentRepository = Objects.requireNonNull(currentRepository);
        this.historyRepository = Objects.requireNonNull(historyRepository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    @Transactional
    public void save(Availability availability) {
        final String status = availability.status().name();
        final AvailabilityJpaEntity current = currentRepository
                .findByFacilityIdAndServiceCode(availability.facilityId(), availability.serviceCode())
                .orElseGet(() -> new AvailabilityJpaEntity(
                        availability.facilityId(), availability.serviceCode(), status, availability.updatedAt()));
        current.applyUpdate(status, availability.updatedAt());
        currentRepository.save(current);

        historyRepository.save(new AvailabilityHistoryJpaEntity(
                availability.facilityId(), availability.serviceCode(), status, availability.updatedAt()));
    }

    @Override
    public List<Availability> findByFacility(UUID facilityId) {
        return currentRepository.findByFacilityIdOrderByServiceCodeAsc(facilityId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Availability> history(UUID facilityId, String serviceCode, int limit) {
        return historyRepository
                .findByFacilityIdAndServiceCodeOrderByUpdatedAtDesc(
                        facilityId, serviceCode, PageRequest.of(0, limit))
                .stream()
                .map(entry -> Availability.of(
                        facilityId, serviceCode,
                        AvailabilityStatus.valueOf(entry.getStatus()),
                        entry.getUpdatedAt()))
                .toList();
    }
}
