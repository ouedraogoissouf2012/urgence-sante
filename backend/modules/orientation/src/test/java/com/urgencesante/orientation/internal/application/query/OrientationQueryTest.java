package com.urgencesante.orientation.internal.application.query;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import org.junit.jupiter.api.Test;

/** Frontières de validation de la requête d'orientation (bornes serveur). */
class OrientationQueryTest {

    private static OrientationQuery query(
            double lat, double lon, String code, int radius, int limit) {
        return new OrientationQuery(lat, lon, code, radius, limit);
    }

    @Test
    void accepte_les_bornes_exactes() {
        assertThatCode(() -> query(90, -180, "maternity",
                OrientationQuery.MAX_RADIUS_METERS, OrientationQuery.MAX_LIMIT))
                .doesNotThrowAnyException();
        assertThatCode(() -> query(-90, 180, "x", 1, 1)).doesNotThrowAnyException();
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
