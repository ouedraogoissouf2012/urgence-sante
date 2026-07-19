package com.urgencesante.medicalservice.internal.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.medicalservice.internal.adapter.in.web.mapper.MedicalServiceWebMapper;
import com.urgencesante.medicalservice.internal.application.port.in.ListMedicalServicesUseCase;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import com.urgencesante.medicalservice.internal.domain.model.MedicalServiceCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MedicalServiceControllerTest {

    @Mock
    private ListMedicalServicesUseCase listMedicalServices;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final MedicalServiceController controller =
                new MedicalServiceController(listMedicalServices, new MedicalServiceWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MedicalServiceExceptionHandler())
                .build();
    }

    @Test
    void liste_le_catalogue() throws Exception {
        given(listMedicalServices.list(any())).willReturn(List.of(
                MedicalService.of(MedicalServiceCode.of("maternity"), "Maternité", "maternal")));

        mockMvc.perform(get("/api/v1/medical-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("maternity"))
                .andExpect(jsonPath("$[0].label").value("Maternité"))
                .andExpect(jsonPath("$[0].category").value("maternal"));
    }

    @Test
    void retourne_400_si_categorie_trop_longue() throws Exception {
        mockMvc.perform(get("/api/v1/medical-services").param("category", "x".repeat(65)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"));
    }
}
