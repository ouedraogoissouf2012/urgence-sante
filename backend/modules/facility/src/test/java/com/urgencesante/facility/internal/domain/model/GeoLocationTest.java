package com.urgencesante.facility.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import org.junit.jupiter.api.Test;

class GeoLocationTest {

    @Test
    void accepte_des_coordonnees_valides() {
        final GeoLocation abidjan = new GeoLocation(5.3599, -4.0083);

        assertThat(abidjan.latitude()).isEqualTo(5.3599);
        assertThat(abidjan.longitude()).isEqualTo(-4.0083);
    }

    @Test
    void refuse_une_latitude_hors_bornes() {
        assertThatThrownBy(() -> new GeoLocation(91.0, 0.0))
                .isInstanceOf(FacilityValidationException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void refuse_une_longitude_hors_bornes() {
        assertThatThrownBy(() -> new GeoLocation(0.0, 181.0))
                .isInstanceOf(FacilityValidationException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    void refuse_les_coordonnees_non_finies() {
        assertThatThrownBy(() -> new GeoLocation(Double.NaN, 0.0))
                .isInstanceOf(FacilityValidationException.class)
                .hasMessageContaining("non finies");
        assertThatThrownBy(() -> new GeoLocation(0.0, Double.POSITIVE_INFINITY))
                .isInstanceOf(FacilityValidationException.class);
    }
}
