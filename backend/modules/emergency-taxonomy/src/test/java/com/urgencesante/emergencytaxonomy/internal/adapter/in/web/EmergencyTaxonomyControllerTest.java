package com.urgencesante.emergencytaxonomy.internal.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.emergencytaxonomy.internal.adapter.in.web.mapper.EmergencyTaxonomyWebMapper;
import com.urgencesante.emergencytaxonomy.internal.application.port.in.GetEmergencyTaxonomyUseCase;
import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import com.urgencesante.emergencytaxonomy.internal.domain.model.Symptom;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EmergencyTaxonomyControllerTest {

    @Mock
    private GetEmergencyTaxonomyUseCase getEmergencyTaxonomy;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final EmergencyTaxonomyController controller =
                new EmergencyTaxonomyController(getEmergencyTaxonomy, new EmergencyTaxonomyWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void expose_la_taxonomie_avec_le_message_d_appel_direct_le_cas_echeant() throws Exception {
        given(getEmergencyTaxonomy.categories()).willReturn(List.of(
                EmergencyCategory.of("respiratoires", "Urgences respiratoires", 1, false, null,
                        List.of(Symptom.of("crise-asthme", "Crise d'asthme")),
                        List.of("pulmonology", "emergency")),
                EmergencyCategory.of("accidents", "Accidents et traumatologie", 4, true,
                        "Appelez les secours.",
                        List.of(Symptom.of("fracture", "Fracture")),
                        List.of("ortho_trauma", "emergency"))));

        mockMvc.perform(get("/api/v1/emergency-taxonomy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("respiratoires"))
                .andExpect(jsonPath("$[0].directCallOnly").value(false))
                .andExpect(jsonPath("$[0].directCallMessage").doesNotExist())
                .andExpect(jsonPath("$[0].symptoms[0].id").value("crise-asthme"))
                .andExpect(jsonPath("$[0].serviceCodes[0]").value("pulmonology"))
                .andExpect(jsonPath("$[1].id").value("accidents"))
                .andExpect(jsonPath("$[1].directCallOnly").value(true))
                .andExpect(jsonPath("$[1].directCallMessage").value("Appelez les secours."));
    }
}
