package com.urgencesante.availability.internal.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.availability.internal.adapter.in.web.mapper.AvailabilityWebMapper;
import com.urgencesante.availability.internal.application.port.in.GetFacilityAvailabilityUseCase;
import com.urgencesante.availability.internal.application.port.in.UpdateAvailabilityUseCase;
import com.urgencesante.availability.internal.application.result.FacilityAvailabilitySnapshot;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;
import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import com.urgencesante.availability.internal.domain.model.Freshness;
import java.time.Instant;
import java.util.List;
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
class AvailabilityControllerTest {

    private static final UUID FACILITY = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private GetFacilityAvailabilityUseCase getFacilityAvailability;

    @Mock
    private UpdateAvailabilityUseCase updateAvailability;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final AvailabilityController controller = new AvailabilityController(
                getFacilityAvailability, updateAvailability, new AvailabilityWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AvailabilityExceptionHandler())
                .build();
    }

    @Test
    void consulte_la_disponibilite() throws Exception {
        given(getFacilityAvailability.forFacility(FACILITY)).willReturn(new FacilityAvailabilitySnapshot(
                FACILITY,
                List.of(new ServiceAvailabilitySnapshot(
                        "maternity", AvailabilityStatus.LIMITED, Freshness.FRESH, Instant.parse("2026-01-01T12:00:00Z")))));

        mockMvc.perform(get("/api/v1/facilities/{id}/availability", FACILITY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services[0].serviceCode").value("maternity"))
                .andExpect(jsonPath("$.services[0].status").value("LIMITED"))
                .andExpect(jsonPath("$.services[0].freshness").value("FRESH"));
    }

    @Test
    void met_a_jour_le_statut() throws Exception {
        given(updateAvailability.update(any())).willReturn(new ServiceAvailabilitySnapshot(
                "maternity", AvailabilityStatus.AVAILABLE, Freshness.FRESH, Instant.parse("2026-01-01T12:00:00Z")));

        mockMvc.perform(put("/api/v1/facilities/{id}/availability/{svc}", FACILITY, "maternity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"AVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void refuse_un_identifiant_invalide() throws Exception {
        mockMvc.perform(get("/api/v1/facilities/{id}/availability", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"));
    }
}
