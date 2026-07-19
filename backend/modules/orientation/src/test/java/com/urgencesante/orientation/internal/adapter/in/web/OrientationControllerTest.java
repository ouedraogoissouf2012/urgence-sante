package com.urgencesante.orientation.internal.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.orientation.internal.adapter.in.web.mapper.OrientationWebMapper;
import com.urgencesante.orientation.internal.application.port.in.RecommendFacilitiesUseCase;
import com.urgencesante.orientation.internal.domain.model.Recommendation;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrientationControllerTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private RecommendFacilitiesUseCase recommendFacilities;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrientationController(recommendFacilities, new OrientationWebMapper()))
                .setControllerAdvice(new OrientationExceptionHandler())
                .build();
    }

    @Test
    void retourne_les_recommandations_classees() throws Exception {
        given(recommendFacilities.recommend(any())).willReturn(List.of(new Recommendation(
                ID, "CHU de Cocody", 5.3496, -3.9851, "+2250100000001",
                1234.5, 600.0,
                com.urgencesante.orientation.internal.domain.model.TravelTimeQuality.REAL,
                "AVAILABLE", 160.0,
                "service disponible · à 1.2 km (~10 min)")));

        mockMvc.perform(get("/api/v1/orientation")
                        .param("lat", "5.35").param("lon", "-4.0").param("service", "maternity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityId").value(ID.toString()))
                .andExpect(jsonPath("$[0].name").value("CHU de Cocody"))
                .andExpect(jsonPath("$[0].location.latitude").value(5.3496))
                .andExpect(jsonPath("$[0].location.longitude").value(-3.9851))
                .andExpect(jsonPath("$[0].phone").value("+2250100000001"))
                .andExpect(jsonPath("$[0].travelTimeQuality").value("REAL"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].explanation").value(
                        "service disponible · à 1.2 km (~10 min)"));
    }

    @Test
    void retourne_400_si_latitude_invalide() throws Exception {
        mockMvc.perform(get("/api/v1/orientation")
                        .param("lat", "200").param("lon", "-4.0").param("service", "maternity"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"));
    }

    @Test
    void retourne_400_si_limite_au_dela_du_maximum() throws Exception {
        mockMvc.perform(get("/api/v1/orientation")
                        .param("lat", "5.35").param("lon", "-4.0")
                        .param("service", "maternity").param("limit", "21"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"));
    }

    @Test
    void retourne_400_si_rayon_au_dela_du_maximum() throws Exception {
        mockMvc.perform(get("/api/v1/orientation")
                        .param("lat", "5.35").param("lon", "-4.0")
                        .param("service", "maternity").param("radiusMeters", "100001"))
                .andExpect(status().isBadRequest());
    }
}
