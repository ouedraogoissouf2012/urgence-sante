package com.urgencesante.orientation.internal.application.service;

import com.urgencesante.orientation.internal.application.port.in.RecommendFacilitiesUseCase;
import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort;
import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort.ServiceStatus;
import com.urgencesante.orientation.internal.application.port.out.CandidateFacilityPort;
import com.urgencesante.orientation.internal.application.port.out.CandidateFacilityPort.CandidateFacility;
import com.urgencesante.orientation.internal.application.port.out.ServiceCatalogPort;
import com.urgencesante.orientation.internal.application.port.out.TravelTimePort;
import com.urgencesante.orientation.internal.application.query.OrientationQuery;
import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import com.urgencesante.orientation.internal.domain.model.CandidateEvaluation;
import com.urgencesante.orientation.internal.domain.model.GeoDistance;
import com.urgencesante.orientation.internal.domain.model.Recommendation;
import com.urgencesante.orientation.internal.domain.model.ScoreContribution;
import com.urgencesante.orientation.internal.domain.strategy.OrientationStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Moteur d'orientation. Compose des stratégies (injectées) pour classer les
 * candidats sans être modifié à l'ajout d'une stratégie (principe ouvert/fermé).
 *
 * <p>Un statut de disponibilité périmé (fraîcheur STALE) est ramené à
 * « UNKNOWN » : l'information n'est pas confirmée. Le fonctionnement reste
 * dégradé mais utile si la disponibilité ou le temps de trajet manquent.
 */
public class OrientationService implements RecommendFacilitiesUseCase {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String STALE = "STALE";

    private final ServiceCatalogPort serviceCatalog;
    private final CandidateFacilityPort candidateFacilityPort;
    private final AvailabilityLookupPort availabilityLookupPort;
    private final TravelTimePort travelTimePort;
    private final List<OrientationStrategy> strategies;

    public OrientationService(
            ServiceCatalogPort serviceCatalog,
            CandidateFacilityPort candidateFacilityPort,
            AvailabilityLookupPort availabilityLookupPort,
            TravelTimePort travelTimePort,
            List<OrientationStrategy> strategies) {
        this.serviceCatalog = Objects.requireNonNull(serviceCatalog);
        this.candidateFacilityPort = Objects.requireNonNull(candidateFacilityPort);
        this.availabilityLookupPort = Objects.requireNonNull(availabilityLookupPort);
        this.travelTimePort = Objects.requireNonNull(travelTimePort);
        this.strategies = List.copyOf(Objects.requireNonNull(strategies));
    }

    @Override
    public List<Recommendation> recommend(OrientationQuery query) {
        if (!serviceCatalog.exists(query.serviceCode())) {
            throw new OrientationValidationException("Service médical inconnu : " + query.serviceCode());
        }
        return candidateFacilityPort.findCandidates(
                        query.serviceCode(), query.latitude(), query.longitude(),
                        query.radiusMeters(), query.limit())
                .stream()
                .map(candidate -> evaluate(query, candidate))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingDouble(Recommendation::score).reversed()
                        .thenComparingDouble(Recommendation::distanceMeters))
                .limit(query.limit())
                .toList();
    }

    private Optional<Recommendation> evaluate(OrientationQuery query, CandidateFacility candidate) {
        final Optional<ServiceStatus> availability =
                availabilityLookupPort.lookup(candidate.facilityId(), query.serviceCode());
        final String freshness = availability.map(ServiceStatus::freshness).orElse(UNKNOWN);
        final String rawStatus = availability.map(ServiceStatus::status).orElse(UNKNOWN);
        // Information périmée = non confirmée.
        final String effectiveStatus = STALE.equals(freshness) ? UNKNOWN : rawStatus;

        final double distance = GeoDistance.meters(
                query.latitude(), query.longitude(), candidate.latitude(), candidate.longitude());
        final OptionalDouble travelTime = travelTimePort.travelTimeSeconds(
                query.latitude(), query.longitude(), candidate.latitude(), candidate.longitude());
        final Double travelSeconds = travelTime.isPresent() ? travelTime.getAsDouble() : null;

        final CandidateEvaluation evaluation = new CandidateEvaluation(
                candidate.facilityId(), candidate.name(), candidate.latitude(), candidate.longitude(),
                distance, effectiveStatus, freshness, travelSeconds);

        double totalScore = 0.0;
        final List<String> reasons = new ArrayList<>();
        for (final OrientationStrategy strategy : strategies) {
            final ScoreContribution contribution = strategy.evaluate(evaluation);
            if (!contribution.eligible()) {
                return Optional.empty();
            }
            totalScore += contribution.score();
            reasons.add(contribution.reason());
        }

        return Optional.of(new Recommendation(
                candidate.facilityId(), candidate.name(),
                candidate.latitude(), candidate.longitude(), candidate.phone(),
                distance, travelSeconds,
                effectiveStatus, totalScore, String.join(" · ", reasons)));
    }
}
