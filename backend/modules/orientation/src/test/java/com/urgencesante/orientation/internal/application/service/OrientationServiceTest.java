package com.urgencesante.orientation.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.orientation.internal.application.port.out.AvailabilityLookupPort;
import com.urgencesante.orientation.internal.application.port.out.CandidateFacilityPort;
import com.urgencesante.orientation.internal.application.query.OrientationQuery;
import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import com.urgencesante.orientation.internal.domain.model.Recommendation;
import com.urgencesante.orientation.internal.domain.strategy.AvailabilityStrategy;
import com.urgencesante.orientation.internal.domain.strategy.ProximityStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Le moteur est testé avec de faux ports en mémoire : catalogue, candidats,
 * disponibilité et temps de trajet contrôlés par le test.
 */
class OrientationServiceTest {

    private static final OrientationQuery QUERY =
            new OrientationQuery(5.35, -4.00, "maternity", 15_000, 5);

    private final List<CandidateFacilityPort.CandidateFacility> candidates = new ArrayList<>();
    private final Map<UUID, AvailabilityLookupPort.ServiceStatus> statuses = new HashMap<>();
    private boolean travelTimeAvailable = true;
    private int travelTimeCalls = 0;

    private final OrientationService service = new OrientationService(
            serviceCode -> !"unknown-service".equals(serviceCode),
            (serviceCode, lat, lon, radius, limit) -> List.copyOf(candidates),
            (facilityId, serviceCode) -> Optional.ofNullable(statuses.get(facilityId)),
            (fromLat, fromLon, destinations) -> {
                travelTimeCalls++;
                return destinations.stream()
                        .map(d -> travelTimeAvailable
                                ? OptionalDouble.of(600.0)
                                : OptionalDouble.empty())
                        .toList();
            },
            List.of(new AvailabilityStrategy(), new ProximityStrategy()));

    private UUID givenCandidate(String name, double lat, double lon, String status, String freshness) {
        final UUID id = UUID.randomUUID();
        candidates.add(new CandidateFacilityPort.CandidateFacility(id, name, lat, lon, "+22501000000"));
        if (status != null) {
            statuses.put(id, new AvailabilityLookupPort.ServiceStatus(status, freshness));
        }
        return id;
    }

    @Test
    void classe_le_disponible_avant_le_sature_a_distance_egale() {
        givenCandidate("Saturé", 5.36, -4.00, "SATURATED", "FRESH");
        final UUID available = givenCandidate("Disponible", 5.36, -4.00, "AVAILABLE", "FRESH");

        final List<Recommendation> result = service.recommend(QUERY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).facilityId()).isEqualTo(available);
        assertThat(result.get(0).explanation()).contains("service disponible");
    }

    @Test
    void un_statut_perime_est_traite_comme_non_confirme() {
        givenCandidate("Périmé", 5.36, -4.00, "AVAILABLE", "STALE");

        final List<Recommendation> result = service.recommend(QUERY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("UNKNOWN");
        assertThat(result.get(0).explanation()).contains("non confirmée");
    }

    @Test
    void un_service_ferme_est_exclu() {
        givenCandidate("Fermé", 5.36, -4.00, "CLOSED", "FRESH");
        final UUID open = givenCandidate("Ouvert", 5.40, -4.00, "AVAILABLE", "FRESH");

        final List<Recommendation> result = service.recommend(QUERY);

        assertThat(result).extracting(Recommendation::facilityId).containsExactly(open);
    }

    @Test
    void fonctionne_en_degrade_sans_disponibilite_ni_trajet() {
        travelTimeAvailable = false;
        givenCandidate("Sans données", 5.36, -4.00, null, null);

        final List<Recommendation> result = service.recommend(QUERY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("UNKNOWN");
        assertThat(result.get(0).travelTimeSeconds()).isNull();
        assertThat(result.get(0).travelTimeQuality())
                .isEqualTo(com.urgencesante.orientation.internal.domain.model.TravelTimeQuality.ESTIMATED);
        assertThat(result.get(0).explanation()).contains("estimé");
    }

    @Test
    void un_seul_appel_de_trajet_quel_que_soit_le_nombre_de_candidats() {
        for (int i = 0; i < 5; i++) {
            givenCandidate("Centre " + i, 5.30 + i * 0.01, -4.00, "AVAILABLE", "FRESH");
        }

        final List<Recommendation> result = service.recommend(QUERY);

        assertThat(result).hasSize(5);
        assertThat(travelTimeCalls)
                .as("la latence ne doit pas dépendre du nombre de candidats")
                .isEqualTo(1);
        assertThat(result.get(0).travelTimeQuality())
                .isEqualTo(com.urgencesante.orientation.internal.domain.model.TravelTimeQuality.REAL);
    }

    @Test
    void le_classement_est_deterministe_pour_des_entrees_identiques() {
        givenCandidate("A", 5.36, -4.00, "LIMITED", "FRESH");
        givenCandidate("B", 5.37, -4.00, "AVAILABLE", "FRESH");
        givenCandidate("C", 5.38, -4.00, "AVAILABLE", "AGING");

        final List<Recommendation> first = service.recommend(QUERY);
        final List<Recommendation> second = service.recommend(QUERY);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void refuse_un_service_inconnu_du_catalogue() {
        assertThatThrownBy(() -> service.recommend(
                new OrientationQuery(5.35, -4.00, "unknown-service", 15_000, 5)))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("inconnu");
    }
}
