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
import com.urgencesante.orientation.internal.domain.model.AvailabilityRating;
import com.urgencesante.orientation.internal.domain.model.CandidateEvaluation;
import com.urgencesante.orientation.internal.domain.model.GeoDistance;
import com.urgencesante.orientation.internal.domain.model.Recommendation;
import com.urgencesante.orientation.internal.domain.model.ScoreContribution;
import com.urgencesante.orientation.internal.domain.model.TravelTimeQuality;
import com.urgencesante.orientation.internal.domain.strategy.OrientationStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Moteur d'orientation. Compose des stratégies (injectées) pour classer les
 * candidats sans être modifié à l'ajout d'une stratégie (principe ouvert/fermé).
 *
 * <p>Les temps de trajet sont obtenus en UN SEUL appel groupé : la latence ne
 * dépend pas du nombre de candidats, et une panne du fournisseur bascule en
 * mode dégradé déterministe (temps estimé depuis la distance, qualifié comme
 * tel). La disponibilité de TOUS les candidats est de même consultée en UN
 * SEUL appel groupé (issue #127), et non un par candidat (et non plus, avant
 * #110, un par (candidat × service)). Un statut périmé (STALE) est ramené à
 * « UNKNOWN ».
 */
public class OrientationService implements RecommendFacilitiesUseCase {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String STALE = "STALE";

    // Vivier de candidats évalués = min(limit × facteur, plafond). Le classement
    // final pondère la disponibilité (jusqu'à 100) au-delà de la proximité (max
    // 60) : un centre disponible juste au-delà des `limit` plus proches doit
    // pouvoir remonter en tête. On évalue donc un vivier plus large que le nombre
    // demandé, borné pour ne pas alourdir routage et disponibilité (issue #112).
    private static final int CANDIDATE_POOL_FACTOR = 3;
    private static final int MAX_CANDIDATE_POOL = 30;

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
        for (final String serviceCode : query.serviceCodes()) {
            if (!serviceCatalog.exists(serviceCode)) {
                throw new OrientationValidationException("Service médical inconnu : " + serviceCode);
            }
        }
        final int poolSize = Math.min(query.limit() * CANDIDATE_POOL_FACTOR, MAX_CANDIDATE_POOL);
        final List<CandidateFacility> candidates = candidateFacilityPort.findCandidates(
                query.serviceCodes(), query.latitude(), query.longitude(),
                query.radiusMeters(), poolSize);
        if (candidates.isEmpty()) {
            return List.of();
        }

        // UN appel groupé pour tous les temps de trajet.
        final List<double[]> destinations = candidates.stream()
                .map(candidate -> new double[] {candidate.latitude(), candidate.longitude()})
                .toList();
        final List<OptionalDouble> travelTimes = travelTimePort.travelTimesSeconds(
                query.latitude(), query.longitude(), destinations);

        // UN appel groupé pour la disponibilité de TOUS les candidats (issue #127 :
        // jusqu'à 30 accès distincts auparavant, un par candidat évalué).
        final Set<UUID> facilityIds = candidates.stream()
                .map(CandidateFacility::facilityId)
                .collect(Collectors.toSet());
        final Map<UUID, List<ServiceStatus>> availabilityByFacility =
                availabilityLookupPort.statusesForFacilities(facilityIds, query.serviceCodes());

        final List<Recommendation> recommendations = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            final OptionalDouble travelTime = i < travelTimes.size()
                    ? travelTimes.get(i)
                    : OptionalDouble.empty();
            evaluate(query, candidates.get(i), travelTime, availabilityByFacility)
                    .ifPresent(recommendations::add);
        }
        recommendations.sort(Comparator.comparingDouble(Recommendation::score).reversed()
                .thenComparingDouble(Recommendation::distanceMeters));
        return recommendations.size() > query.limit()
                ? List.copyOf(recommendations.subList(0, query.limit()))
                : List.copyOf(recommendations);
    }

    private Optional<Recommendation> evaluate(
            OrientationQuery query, CandidateFacility candidate, OptionalDouble travelTime,
            Map<UUID, List<ServiceStatus>> availabilityByFacility) {
        final Optional<ServiceStatus> availability = mostFavorableStatus(
                availabilityByFacility.getOrDefault(candidate.facilityId(), List.of()));
        final String freshness = availability.map(ServiceStatus::freshness).orElse(UNKNOWN);
        final String rawStatus = availability.map(ServiceStatus::status).orElse(UNKNOWN);
        // Information périmée = non confirmée.
        final String effectiveStatus = STALE.equals(freshness) ? UNKNOWN : rawStatus;

        final double distance = GeoDistance.meters(
                query.latitude(), query.longitude(), candidate.latitude(), candidate.longitude());
        final Double travelSeconds = travelTime.isPresent() ? travelTime.getAsDouble() : null;
        final TravelTimeQuality quality =
                travelSeconds != null ? TravelTimeQuality.REAL : TravelTimeQuality.ESTIMATED;

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
                distance, travelSeconds, quality,
                effectiveStatus, totalScore, String.join(" · ", reasons)));
    }

    /**
     * Meilleur statut de disponibilité parmi les services demandés : un centre
     * est aussi favorable que son meilleur service pertinent. L'ordre reflète le
     * scoring d'{@code AvailabilityStrategy} (AVAILABLE > LIMITED > non confirmé
     * > SATURATED > CLOSED) ; un statut périmé (STALE) est ramené à non confirmé.
     */
    private Optional<ServiceStatus> mostFavorableStatus(List<ServiceStatus> statuses) {
        ServiceStatus best = null;
        int bestRank = -1;
        for (final ServiceStatus status : statuses) {
            // Rang = ordinal du rating (source unique de l'ordre de préférence).
            final int rank = AvailabilityRating
                    .fromRaw(status.status(), status.freshness())
                    .ordinal();
            if (rank > bestRank) {
                bestRank = rank;
                best = status;
            }
        }
        return Optional.ofNullable(best);
    }
}
