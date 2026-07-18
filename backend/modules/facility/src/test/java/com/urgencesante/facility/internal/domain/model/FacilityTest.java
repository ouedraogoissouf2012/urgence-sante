package com.urgencesante.facility.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.facility.internal.domain.exception.FacilityValidationException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FacilityTest {

    private static final FacilityId AN_ID = FacilityId.of(UUID.randomUUID());
    private static final GeoLocation A_LOCATION = new GeoLocation(5.35, -4.00);

    @Test
    void construit_un_etablissement_valide_et_normalise_le_nom() {
        final Facility facility = Facility.of(
                AN_ID, "  CHU de Cocody  ", A_LOCATION, "+2250700000000",
                Set.of(MedicalServiceCode.of("Maternity")));

        assertThat(facility.name()).isEqualTo("CHU de Cocody");
        assertThat(facility.phone()).contains("+2250700000000");
        assertThat(facility.offers(MedicalServiceCode.of("maternity"))).isTrue();
        assertThat(facility.offers(MedicalServiceCode.of("surgery"))).isFalse();
    }

    @Test
    void refuse_un_nom_vide() {
        assertThatThrownBy(() -> Facility.of(
                AN_ID, "   ", A_LOCATION, null, Set.of()))
                .isInstanceOf(FacilityValidationException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void traite_un_telephone_vide_comme_absent() {
        final Facility facility = Facility.of(AN_ID, "Clinique", A_LOCATION, "  ", Set.of());

        assertThat(facility.phone()).isEmpty();
    }

    @Test
    void expose_des_services_immuables() {
        final Set<MedicalServiceCode> mutable = new LinkedHashSet<>();
        mutable.add(MedicalServiceCode.of("pediatrics"));
        final Facility facility = Facility.of(AN_ID, "Clinique", A_LOCATION, null, mutable);

        // Modifier la source après coup n'affecte pas l'agrégat.
        mutable.add(MedicalServiceCode.of("surgery"));
        assertThat(facility.services()).hasSize(1);
        assertThatThrownBy(() -> facility.services().add(MedicalServiceCode.of("x")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void l_egalite_repose_sur_l_identite() {
        final Facility a = Facility.of(AN_ID, "Nom A", A_LOCATION, null, Set.of());
        final Facility b = Facility.of(AN_ID, "Nom B", new GeoLocation(0, 0), "123", Set.of());

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
