package com.urgencesante.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vérifie le filet des erreurs de liaison de paramètres du framework (audit
 * P3 #140, point 7) sur un contrôleur factice — sans dépendre d'un module
 * métier réel, ni de la base. Le filet des exceptions vraiment inattendues
 * (point 4) est un mécanisme séparé : voir {@link LastResortExceptionResolverTest}.
 */
class GlobalExceptionHandlerTest {

    @RestController
    static class DummyController {
        @GetMapping("/dummy")
        String withRequiredParam(@RequestParam double lat) {
            return "ok " + lat;
        }

        @PostMapping("/dummy")
        String withBody(@RequestBody DummyBody body) {
            return "ok " + body.value();
        }
    }

    record DummyBody(String value) {
    }

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void parametre_de_mauvais_type_devient_400_problem_json() throws Exception {
        final String body = mockMvc.perform(get("/dummy").param("lat", "abc"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"title\":\"Requête invalide\"").contains("lat");
    }

    @Test
    void parametre_requis_absent_devient_400_problem_json() throws Exception {
        final String body = mockMvc.perform(get("/dummy"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"title\":\"Requête invalide\"").contains("lat");
    }

    @Test
    void corps_json_illisible_devient_400_problem_json() throws Exception {
        mockMvc.perform(post("/dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ceci n'est pas du json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void les_reponses_du_filet_sont_bien_du_problem_json() throws Exception {
        mockMvc.perform(get("/dummy").param("lat", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }
}
