package com.urgencesante.routing.internal.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.routing.internal.adapter.in.web.mapper.RoutingWebMapper;
import com.urgencesante.routing.internal.application.port.in.GetRouteUseCase;
import com.urgencesante.routing.internal.domain.model.Route;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RoutingControllerTest {

    @Mock
    private GetRouteUseCase getRoute;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RoutingController(getRoute, new RoutingWebMapper()))
                .setControllerAdvice(new RoutingExceptionHandler())
                .build();
    }

    @Test
    void retourne_l_itineraire() throws Exception {
        given(getRoute.route(any(), any())).willReturn(Optional.of(new Route(1500.0, 240.0)));

        mockMvc.perform(get("/api/v1/routes")
                        .param("fromLat", "5.35").param("fromLon", "-4.0")
                        .param("toLat", "5.30").param("toLon", "-4.05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(1500.0))
                .andExpect(jsonPath("$.durationSeconds").value(240.0));
    }

    @Test
    void retourne_404_si_aucun_itineraire() throws Exception {
        given(getRoute.route(any(), any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/routes")
                        .param("fromLat", "5.35").param("fromLon", "-4.0")
                        .param("toLat", "5.30").param("toLon", "-4.05"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Itinéraire introuvable"));
    }

    @Test
    void retourne_400_si_coordonnees_invalides() throws Exception {
        mockMvc.perform(get("/api/v1/routes")
                        .param("fromLat", "200").param("fromLon", "-4.0")
                        .param("toLat", "5.30").param("toLon", "-4.05"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"));
    }
}
