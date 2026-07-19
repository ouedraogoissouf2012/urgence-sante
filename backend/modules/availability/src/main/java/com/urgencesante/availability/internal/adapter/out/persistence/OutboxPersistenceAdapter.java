package com.urgencesante.availability.internal.adapter.out.persistence;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.adapter.out.persistence.entity.OutboxJpaEntity;
import com.urgencesante.availability.internal.adapter.out.persistence.repository.OutboxSpringRepository;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur de persistance de l'outbox. */
@Component
public class OutboxPersistenceAdapter implements OutboxPort {

    private final OutboxSpringRepository repository;
    private final Clock clock;

    public OutboxPersistenceAdapter(OutboxSpringRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void append(AvailabilityUpdated event) {
        // Participe à la transaction ouverte par le cas d'usage.
        repository.save(OutboxJpaEntity.from(event));
    }

    @Override
    public List<AvailabilityUpdated> unpublished(int limit) {
        return repository.findByPublishedAtIsNullOrderByOccurredAtAsc(PageRequest.of(0, limit))
                .stream()
                .map(OutboxJpaEntity::toEvent)
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId) {
        repository.findById(eventId).ifPresent(entity -> {
            if (entity.getPublishedAt() == null) {
                entity.markPublished(clock.instant());
                repository.save(entity);
            }
        });
    }

    @Override
    @Transactional
    public void recordFailure(UUID eventId) {
        repository.findById(eventId).ifPresent(entity -> {
            entity.recordFailure();
            repository.save(entity);
        });
    }
}
