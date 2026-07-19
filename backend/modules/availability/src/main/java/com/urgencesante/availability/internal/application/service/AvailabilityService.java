package com.urgencesante.availability.internal.application.service;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.in.GetAvailabilityHistoryUseCase;
import com.urgencesante.availability.internal.application.port.in.GetFacilityAvailabilityUseCase;
import com.urgencesante.availability.internal.application.port.in.UpdateAvailabilityUseCase;
import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.OfferedServicePort;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.TransactionPort;
import com.urgencesante.availability.internal.application.result.AvailabilityHistoryEntry;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import com.urgencesante.availability.internal.domain.exception.ServiceNotOfferedException;
import com.urgencesante.availability.internal.domain.model.Availability;
import com.urgencesante.availability.internal.domain.policy.FreshnessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Cas d'usage de disponibilité. Java pur : horloge, politique de fraîcheur et
 * frontière transactionnelle sont injectées par des ports.
 *
 * <p>Fiabilité (issue #43) : la disponibilité courante, l'historique ET
 * l'événement (outbox) sont écrits dans LA MÊME transaction pilotée ici ; la
 * publication est assurée ensuite par un relais avec reprise — une panne de
 * publication ne perd jamais l'événement.
 */
public class AvailabilityService
        implements UpdateAvailabilityUseCase, GetFacilityAvailabilityUseCase, GetAvailabilityHistoryUseCase {

    private final SaveAvailabilityPort saveAvailabilityPort;
    private final LoadAvailabilityPort loadAvailabilityPort;
    private final OfferedServicePort offeredServicePort;
    private final OutboxPort outboxPort;
    private final TransactionPort transactionPort;
    private final Clock clock;
    private final FreshnessPolicy freshnessPolicy;

    public AvailabilityService(
            SaveAvailabilityPort saveAvailabilityPort,
            LoadAvailabilityPort loadAvailabilityPort,
            OfferedServicePort offeredServicePort,
            OutboxPort outboxPort,
            TransactionPort transactionPort,
            Clock clock,
            FreshnessPolicy freshnessPolicy) {
        this.saveAvailabilityPort = Objects.requireNonNull(saveAvailabilityPort);
        this.loadAvailabilityPort = Objects.requireNonNull(loadAvailabilityPort);
        this.offeredServicePort = Objects.requireNonNull(offeredServicePort);
        this.outboxPort = Objects.requireNonNull(outboxPort);
        this.transactionPort = Objects.requireNonNull(transactionPort);
        this.clock = Objects.requireNonNull(clock);
        this.freshnessPolicy = Objects.requireNonNull(freshnessPolicy);
    }

    @Override
    public ServiceAvailabilitySnapshot update(UpdateAvailabilityCommand command) {
        // Le couple (établissement, service) doit correspondre à une offre réelle.
        if (!offeredServicePort.offers(command.facilityId(), command.serviceCode())) {
            throw new ServiceNotOfferedException(command.facilityId(), command.serviceCode());
        }
        final Instant now = clock.instant();
        final Availability availability =
                Availability.of(command.facilityId(), command.serviceCode(), command.status(), now);
        final UUID eventId = UUID.randomUUID();
        final AvailabilityUpdated event = new AvailabilityUpdated(
                eventId,
                // Corrélation de requête complète prévue avec l'observabilité (#46).
                eventId.toString(),
                availability.facilityId(),
                availability.serviceCode(),
                availability.status().name(),
                availability.updatedAt(),
                now);

        // Frontière transactionnelle du cas d'usage : courant + historique +
        // outbox sont atomiques (tout ou rien).
        transactionPort.inTransaction(() -> {
            saveAvailabilityPort.save(availability);
            outboxPort.append(event);
            return null;
        });

        return toSnapshot(availability, now);
    }

    @Override
    public FacilityAvailabilitySnapshot forFacility(UUID facilityId) {
        Objects.requireNonNull(facilityId, "L'établissement est requis");
        final Instant now = clock.instant();
        final List<ServiceAvailabilitySnapshot> services = loadAvailabilityPort.findByFacility(facilityId).stream()
                .map(availability -> toSnapshot(availability, now))
                .toList();
        return new FacilityAvailabilitySnapshot(facilityId, services);
    }

    @Override
    public List<AvailabilityHistoryEntry> history(UUID facilityId, String serviceCode, int limit) {
        Objects.requireNonNull(facilityId, "L'établissement est requis");
        Objects.requireNonNull(serviceCode, "Le service est requis");
        return loadAvailabilityPort.history(facilityId, serviceCode, limit).stream()
                .map(entry -> new AvailabilityHistoryEntry(entry.status(), entry.updatedAt()))
                .toList();
    }

    private ServiceAvailabilitySnapshot toSnapshot(Availability availability, Instant now) {
        return new ServiceAvailabilitySnapshot(
                availability.serviceCode(),
                availability.status(),
                freshnessPolicy.evaluate(availability.updatedAt(), now),
                availability.updatedAt());
    }
}
