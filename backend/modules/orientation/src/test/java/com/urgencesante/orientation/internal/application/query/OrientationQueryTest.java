package com.urgencesante.orientation.internal.application.query;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
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
}
