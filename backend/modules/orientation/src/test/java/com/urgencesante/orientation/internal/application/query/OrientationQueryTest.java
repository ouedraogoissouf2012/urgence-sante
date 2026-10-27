package com.urgencesante.orientation.internal.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Frontières de validation de la requête d'orientation (bornes serveur). */
class OrientationQueryTest {

    private static OrientationQuery query(
            double lat, double lon, String code, int radius, int limit) {
        return new OrientationQuery(lat, lon, Set.of(code), radius, limit);
    }

    @Test
    void accepte_les_bornes_exactes() {
        assertThatCode(() -> query(90, -180, "maternity",
                OrientationQuery.MAX_RADIUS_METERS, OrientationQuery.MAX_LIMIT))
                .doesNotThrowAnyException();
        assertThatCode(() -> query(-90, 180, "x", 1, 1)).doesNotThrowAnyException();
    }

    @Test
    void accepte_plusieurs_services() {
        assertThatCode(() -> new OrientationQuery(
                5, -4, Set.of("pulmonology", "intensive_care", "emergency"), 1000, 5))
                .doesNotThrowAnyException();
    }

    @Test
    void refuse_un_ensemble_de_services_vide() {
        assertThatThrownBy(() -> new OrientationQuery(5, -4, Set.of(), 1000, 5))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("Au moins un service");
    }

    @Test
    void refuse_trop_de_services() {
        final Set<String> trop = new java.util.HashSet<>();
        for (int i = 0; i <= OrientationQuery.MAX_SERVICE_CODES; i++) {
            trop.add("service-" + i);
        }
        assertThatThrownBy(() -> new OrientationQuery(5, -4, trop, 1000, 5))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("Trop de services");
    }

    @Test
    void refuse_les_coordonnees_non_finies() {
        assertThatThrownBy(() -> query(Double.NaN, 0, "maternity", 1000, 5))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("non finies");
        assertThatThrownBy(() -> query(0, Double.POSITIVE_INFINITY, "maternity", 1000, 5))
                .isInstanceOf(OrientationValidationException.class);
    }

    @Test
    void refuse_un_rayon_hors_bornes() {
        assertThatThrownBy(() -> query(5, -4, "maternity", 0, 5))
                .isInstanceOf(OrientationValidationException.class);
        assertThatThrownBy(() -> query(5, -4, "maternity",
                OrientationQuery.MAX_RADIUS_METERS + 1, 5))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("rayon");
    }

    @Test
    void refuse_une_limite_hors_bornes() {
        assertThatThrownBy(() -> query(5, -4, "maternity", 1000, 0))
                .isInstanceOf(OrientationValidationException.class);
        assertThatThrownBy(() -> query(5, -4, "maternity", 1000, OrientationQuery.MAX_LIMIT + 1))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("limite");
    }

    @Test
    void refuse_un_code_vide_ou_trop_long() {
        assertThatThrownBy(() -> query(5, -4, "   ", 1000, 5))
                .isInstanceOf(OrientationValidationException.class);
        assertThatThrownBy(() -> query(5, -4, "x".repeat(65), 1000, 5))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("64");
    }

    // ── Élargissement du rayon quand aucun centre ne répond au besoin ───────

    private static OrientationQuery widening(int radius, List<Integer> steps) {
        return new OrientationQuery(5.24, -3.90, Set.of("maternity"), radius, 5, steps);
    }

    @Test
    void les_paliers_d_elargissement_suivent_le_rayon_initial() {
        assertThat(widening(15_000, List.of(30_000, 50_000, 70_000)).searchRadiiMeters())
                .containsExactly(15_000, 30_000, 50_000, 70_000);
    }

    @Test
    void sans_palier_seul_le_rayon_demande_est_cherche() {
        assertThat(query(5, -4, "maternity", 15_000, 5).searchRadiiMeters())
                .containsExactly(15_000);
    }

    @Test
    void un_palier_qui_ne_depasse_pas_le_rayon_initial_est_ignore() {
        // Ex. profil local : rayon initial de 100 km, plus large que les paliers.
        assertThat(widening(100_000, List.of(30_000, 70_000)).searchRadiiMeters())
                .containsExactly(100_000);
    }

    @Test
    void refuse_des_paliers_non_croissants_ou_hors_bornes() {
        assertThatThrownBy(() -> widening(15_000, List.of(50_000, 30_000)))
                .isInstanceOf(OrientationValidationException.class)
                .hasMessageContaining("croissant");
        assertThatThrownBy(() -> widening(15_000, List.of(OrientationQuery.MAX_RADIUS_METERS + 1)))
                .isInstanceOf(OrientationValidationException.class);
    }
}
