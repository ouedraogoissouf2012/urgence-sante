package com.urgencesante.routing.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.routing.internal.domain.exception.RoutingValidationException;
import org.junit.jupiter.api.Test;

class CoordinatesTest {

    @Test
    void accepte_les_bornes_exactes() {
        assertThatCode(() -> new Coordinates(90, -180)).doesNotThrowAnyException();
        assertThatCode(() -> new Coordinates(-90, 180)).doesNotThrowAnyException();
    }

    @Test
    void refuse_hors_bornes_et_non_finis() {
        assertThatThrownBy(() -> new Coordinates(90.0001, 0))
                .isInstanceOf(RoutingValidationException.class);
        assertThatThrownBy(() -> new Coordinates(Double.NaN, 0))
                .isInstanceOf(RoutingValidationException.class)
                .hasMessageContaining("non finies");
        assertThatThrownBy(() -> new Coordinates(0, Double.NEGATIVE_INFINITY))
                .isInstanceOf(RoutingValidationException.class);
    }
}
