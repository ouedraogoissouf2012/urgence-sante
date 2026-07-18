package com.urgencesante.facility.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.buildingblocks.pagination.PageRequest;
import com.urgencesante.facility.internal.application.port.out.LoadFacilityPort;
import com.urgencesante.facility.internal.application.query.FindFacilitiesQuery;
import com.urgencesante.facility.internal.domain.exception.FacilityNotFoundException;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FacilityQueryServiceTest {

    /** Faux port de persistance en mémoire, substituable au vrai adaptateur. */
    private final Map<FacilityId, Facility> store = new LinkedHashMap<>();
    private final LoadFacilityPort fakePort = new LoadFacilityPort() {
        @Override
        public Page<Facility> search(FindFacilitiesQuery query) {
            final List<Facility> content = List.copyOf(store.values());
            return new Page<>(content, query.page().page(), query.page().size(), content.size());
        }

        @Override
        public Optional<Facility> findById(FacilityId id) {
            return Optional.ofNullable(store.get(id));
        }
    };
    private final FacilityQueryService service = new FacilityQueryService(fakePort);

    private Facility givenFacility(String name) {
        final Facility facility = Facility.of(
                FacilityId.of(UUID.randomUUID()), name, new GeoLocation(5.35, -4.0), null, Set.of());
        store.put(facility.id(), facility);
        return facility;
    }

    @Test
    void getFacility_retourne_l_etablissement_present() {
        final Facility expected = givenFacility("CHU");

        assertThat(service.getFacility(expected.id())).isEqualTo(expected);
    }

    @Test
    void getFacility_leve_une_erreur_si_absent() {
        final FacilityId unknown = FacilityId.of(UUID.randomUUID());

        assertThatThrownBy(() -> service.getFacility(unknown))
                .isInstanceOf(FacilityNotFoundException.class);
    }

    @Test
    void findFacilities_delegue_au_port_et_pagine() {
        givenFacility("A");
        givenFacility("B");
        final FindFacilitiesQuery query = new FindFacilitiesQuery(
                Optional.empty(), Optional.empty(), Optional.empty(), PageRequest.of(0, 20));

        final Page<Facility> page = service.findFacilities(query);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.number()).isZero();
    }
}
