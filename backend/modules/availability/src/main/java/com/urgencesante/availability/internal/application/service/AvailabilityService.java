package com.urgencesante.availability.internal.application.service;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.in.GetAvailabilityHistoryUseCase;
import com.urgencesante.availability.internal.application.port.in.GetFacilityAvailabilityUseCase;
import com.urgencesante.availability.internal.application.port.in.UpdateAvailabilityUseCase;
import com.urgencesante.availability.internal.application.result.AvailabilityHistoryEntry;
import com.urgencesante.availability.internal.application.port.out.AvailabilityEventPublisher;
import com.urgencesante.availability.internal.application.port.out.LoadAvailabilityPort;
import com.urgencesante.availability.internal.application.port.out.OfferedServicePort;
import com.urgencesante.availability.internal.application.port.out.SaveAvailabilityPort;
import com.urgencesante.availability.internal.domain.exception.ServiceNotOfferedException;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import com.urgencesante.availability.internal.domain.model.Availability;
import com.urgencesante.availability.internal.domain.policy.FreshnessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Cas d'usage de disponibilité. Java pur ; l'horloge et la politique de
 * fraîcheur sont injectées, ce qui rend le comportement temporel testable.
 */
public class AvailabilityService
        implements UpdateAvailabilityUseCase, GetFacilityAvailabilityUseCase, GetAvailabilityHistoryUseCase {

    private final SaveAvailabilityPort saveAvailabilityPort;
    private final LoadAvailabilityPort loadAvailabilityPort;
    private final OfferedServicePort offeredServicePort;
    private final AvailabilityEventPublisher eventPublisher;
    private final Clock clock;
    private final FreshnessPolicy freshnessPolicy;

    public AvailabilityService(
            SaveAvailabilityPort saveAvailabilityPort,
            LoadAvailabilityPort loadAvailabilityPort,
            OfferedServicePort offeredServicePort,
            AvailabilityEventPublisher eventPublisher,
            Clock clock,
            FreshnessPolicy freshnessPolicy) {
        this.saveAvailabilityPort = Objects.requireNonNull(saveAvailabilityPort);
        this.loadAvailabilityPort = Objects.requireNonNull(loadAvailabilityPort);
        this.offeredServicePort = Objects.requireNonNull(offeredServicePort);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
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

        saveAvailabilityPort.save(availability);
        eventPublisher.publish(new AvailabilityUpdated(
                availability.facilityId(),
                availability.serviceCode(),
                availability.status().name(),
                availability.updatedAt()));

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
