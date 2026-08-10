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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Le moteur est testé avec de faux ports en mémoire : catalogue, candidats,
 * disponibilité et temps de trajet contrôlés par le test.
 */
class OrientationServiceTest {

    private static final OrientationQuery QUERY =
            new OrientationQuery(5.35, -4.00, Set.of("maternity"), 15_000, 5);

    private final List<CandidateFacilityPort.CandidateFacility> candidates = new ArrayList<>();
    // Clé : « facilityId » (niveau établissement) ou « facilityId|serviceCode »
    // (par service, pour tester la combinaison multi-services).
    private final Map<String, AvailabilityLookupPort.ServiceStatus> statuses = new HashMap<>();
    private boolean travelTimeAvailable = true;
    private int travelTimeCalls = 0;
    private int availabilityCalls = 0;

    private final OrientationService service = new OrientationService(
            serviceCode -> !"unknown-service".equals(serviceCode),
            // Faux port candidat : renvoie les `limit` plus proches (comme PostGIS),
            // pour pouvoir tester la taille du vivier de candidats évalués.
            (serviceCodes, lat, lon, radius, limit) -> candidates.stream()
                    .sorted(Comparator.comparingDouble(c ->
                            Math.pow(c.latitude() - lat, 2) + Math.pow(c.longitude() - lon, 2)))
                    .limit(limit)
                    .toList(),
            // Faux port disponibilité : UN SEUL appel groupé (compté), quel que soit
            // le nombre d'établissements, qui renvoie les statuts des services
            // demandés pour chacun (par service, sinon niveau établissement).
            (facilityIds, serviceCodes) -> {
                availabilityCalls++;
                final Map<UUID, List<AvailabilityLookupPort.ServiceStatus>> result = new HashMap<>();
                for (final UUID facilityId : facilityIds) {
                    final List<AvailabilityLookupPort.ServiceStatus> found = new ArrayList<>();
                    for (final String serviceCode : serviceCodes) {
                        final AvailabilityLookupPort.ServiceStatus perService =
                                statuses.get(facilityId + "|" + serviceCode);
                        final AvailabilityLookupPort.ServiceStatus status =
                                perService != null ? perService : statuses.get(facilityId.toString());
                        if (status != null) {
                            found.add(status);
                        }
                    }
                    result.put(facilityId, found);
                }
                return result;
            },
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
            statuses.put(id.toString(), new AvailabilityLookupPort.ServiceStatus(status, freshness));
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
                new OrientationQuery(5.35, -4.00, Set.of("unknown-service"), 15_000, 5)))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("inconnu");
    }

    @Test
    void retient_le_meilleur_statut_parmi_les_services_demandes() {
        // Un centre offre deux services demandés : l'un saturé, l'autre disponible.
        // Le meilleur (AVAILABLE) doit l'emporter — le centre est classé disponible.
        final UUID id = UUID.randomUUID();
        candidates.add(new CandidateFacilityPort.CandidateFacility(
                id, "Centre polyvalent", 5.36, -4.00, "+22501000000"));
        statuses.put(id + "|pulmonology", new AvailabilityLookupPort.ServiceStatus("SATURATED", "FRESH"));
        statuses.put(id + "|intensive_care", new AvailabilityLookupPort.ServiceStatus("AVAILABLE", "FRESH"));

        final List<Recommendation> result = service.recommend(new OrientationQuery(
                5.35, -4.00, Set.of("pulmonology", "intensive_care"), 15_000, 5));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("AVAILABLE");
        assertThat(result.get(0).explanation()).contains("service disponible");
    }

    @Test
    void un_seul_appel_de_disponibilite_quel_que_soit_le_nombre_de_services() {
        // Un centre offre trois services demandés : la disponibilité doit être
        // consultée UNE fois pour ce centre, pas une fois par service (issue #110).
        final UUID id = UUID.randomUUID();
        candidates.add(new CandidateFacilityPort.CandidateFacility(
                id, "Centre polyvalent", 5.36, -4.00, "+22501000000"));
        statuses.put(id + "|pulmonology", new AvailabilityLookupPort.ServiceStatus("SATURATED", "FRESH"));
        statuses.put(id + "|neurology", new AvailabilityLookupPort.ServiceStatus("LIMITED", "FRESH"));
        statuses.put(id + "|intensive_care", new AvailabilityLookupPort.ServiceStatus("AVAILABLE", "FRESH"));

        service.recommend(new OrientationQuery(
                5.35, -4.00, Set.of("pulmonology", "neurology", "intensive_care"), 15_000, 5));

        assertThat(availabilityCalls)
                .as("un seul accès à la disponibilité, pas un par service")
                .isEqualTo(1);
    }

    @Test
    void un_seul_appel_de_disponibilite_groupe_quel_que_soit_le_nombre_de_candidats() {
        // Jusqu'à 30 candidats évalués par orientation : la disponibilité de TOUS
        // doit être consultée en UN SEUL appel groupé, pas un par candidat
        // (issue #127 : jusqu'à 30 accès DB distincts auparavant).
        for (int i = 0; i < 10; i++) {
            givenCandidate("Centre " + i, 5.30 + i * 0.001, -4.00, "AVAILABLE", "FRESH");
        }

        final List<Recommendation> result = service.recommend(
                new OrientationQuery(5.35, -4.00, Set.of("maternity"), 15_000, 10));

        assertThat(result).hasSize(10);
        assertThat(availabilityCalls)
                .as("la charge sur la base ne doit pas dépendre du nombre de candidats")
                .isEqualTo(1);
    }

    @Test
    void un_centre_disponible_hors_des_plus_proches_est_tout_de_meme_recommande() {
        // Les `limit` (5) plus proches sont tous saturés ; un centre disponible se
        // trouve juste au-delà. Le score privilégiant la disponibilité, il doit être
        // évalué (vivier élargi) et remonter en tête — sinon il ne serait jamais
        // chargé, la recommandation restant sous-optimale (issue #112).
        for (int i = 0; i < 5; i++) {
            givenCandidate("Proche saturé " + i, 5.351 + i * 0.0001, -4.00, "SATURATED", "FRESH");
        }
        final UUID available =
                givenCandidate("Disponible un peu plus loin", 5.360, -4.00, "AVAILABLE", "FRESH");

        final List<Recommendation> result = service.recommend(
                new OrientationQuery(5.35, -4.00, Set.of("maternity"), 15_000, 5));

        assertThat(result).hasSize(5);
        assertThat(result.get(0).facilityId())
                .as("le centre disponible, hors des 5 plus proches, est recommandé en tête")
                .isEqualTo(available);
    }
}
