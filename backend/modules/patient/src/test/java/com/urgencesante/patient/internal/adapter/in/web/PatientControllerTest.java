package com.urgencesante.patient.internal.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.urgencesante.patient.internal.application.port.in.AuthenticatePatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RegisterPatientUseCase;
import com.urgencesante.patient.internal.application.port.in.RevokePatientSessionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Couche web du module Patient : ici, seule l'extraction du jeton porteur
 * pour la révocation de session (audit P3 #140, point 8) — le reste du
 * contrôleur est déjà couvert par PatientApiIntegrationTest (bout en bout,
 * base réelle).
 */
@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private RegisterPatientUseCase registerPatient;

    @Mock
    private AuthenticatePatientUseCase authenticatePatient;

    @Mock
    private RevokePatientSessionUseCase revokeSession;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final PatientController controller =
                new PatientController(registerPatient, authenticatePatient, revokeSession);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new PatientExceptionHandler())
                .build();
    }

    @Test
    void revoque_la_session_avec_un_jeton_porteur_valide() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/session").header("Authorization", "Bearer un-jeton"))
                .andExpect(status().isNoContent());

        then(revokeSession).should().revoke(eq("un-jeton"));
    }

    @Test
    void refuse_400_si_l_en_tete_authorization_est_absent() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/session"))
                .andExpect(status().isBadRequest());

        then(revokeSession).should(never()).revoke(anyString());
    }

    @Test
    void refuse_400_si_l_en_tete_n_est_pas_de_type_bearer() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/session").header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refuse_400_si_le_jeton_bearer_est_vide() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/session").header("Authorization", "Bearer "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ignore_la_casse_du_prefixe_bearer() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/session").header("Authorization", "bearer un-jeton"))
                .andExpect(status().isNoContent());

        then(revokeSession).should().revoke(eq("un-jeton"));
    }

    @Test
    void la_reponse_400_est_bien_du_problem_json() throws Exception {
        final String body = mockMvc.perform(delete("/api/v1/patients/session"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"title\":\"Requête invalide\"");
    }
}
