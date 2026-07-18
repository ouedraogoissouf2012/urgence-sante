package com.urgencesante.facility.internal.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.buildingblocks.pagination.Page;
import com.urgencesante.facility.internal.adapter.in.web.mapper.FacilityWebMapper;
import com.urgencesante.facility.internal.application.port.in.FindFacilitiesUseCase;
import com.urgencesante.facility.internal.application.port.in.GetFacilityUseCase;
import com.urgencesante.facility.internal.domain.exception.FacilityNotFoundException;
import com.urgencesante.facility.internal.domain.model.Facility;
import com.urgencesante.facility.internal.domain.model.FacilityId;
import com.urgencesante.facility.internal.domain.model.GeoLocation;
import com.urgencesante.facility.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class FacilityControllerTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private FindFacilitiesUseCase findFacilities;

    @Mock
    private GetFacilityUseCase getFacility;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final FacilityController controller =
                new FacilityController(findFacilities, getFacility, new FacilityWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new FacilityExceptionHandler())
                .build();
    }

    private static Facility sampleFacility() {
        return Facility.of(
                FacilityId.of(ID), "CHU de Cocody", new GeoLocation(5.35, -4.0), "+2250700000000",
                Set.of(MedicalServiceCode.of("maternity")));
    }

    @Test
    void liste_les_etablissements_en_page() throws Exception {
        given(findFacilities.findFacilities(any()))
                .willReturn(new Page<>(List.of(sampleFacility()), 0, 20, 1));

        mockMvc.perform(get("/api/v1/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("CHU de Cocody"))
                .andExpect(jsonPath("$.content[0].location.latitude").value(5.35))
                .andExpect(jsonPath("$.content[0].services[0]").value("maternity"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void consulte_un_etablissement() throws Exception {
        given(getFacility.getFacility(FacilityId.of(ID))).willReturn(sampleFacility());

        mockMvc.perform(get("/api/v1/facilities/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.phone").value("+2250700000000"));
    }

    @Test
    void retourne_404_probleme_si_absent() throws Exception {
        given(getFacility.getFacility(any()))
                .willThrow(new FacilityNotFoundException(FacilityId.of(ID)));

        mockMvc.perform(get("/api/v1/facilities/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Établissement introuvable"));
    }

    @Test
    void retourne_400_si_latitude_seule() throws Exception {
        mockMvc.perform(get("/api/v1/facilities").param("lat", "5.35"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"));
    }
}
