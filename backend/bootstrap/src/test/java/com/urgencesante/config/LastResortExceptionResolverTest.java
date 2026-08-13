package com.urgencesante.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vérifie le filet de dernier recours pour les exceptions vraiment
 * inattendues (audit P3 #140, point 4). Enregistré comme
 * {@code HandlerExceptionResolver} (pas {@code @ControllerAdvice}) — voir
 * {@link LastResortExceptionResolver} pour pourquoi. Le test le prouve en
 * l'enregistrant explicitement via {@code setHandlerExceptionResolvers}, PAS
 * {@code setControllerAdvice}.
 */
class LastResortExceptionResolverTest {

    @RestController
    static class DummyController {
        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("détail interne sensible — ne doit jamais fuiter au client");
        }
    }

    private final ErrorResponses errorResponses =
            new ErrorResponses(Jackson2ObjectMapperBuilder.json().build());

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
            .setHandlerExceptionResolvers(new LastResortExceptionResolver(errorResponses))
            .build();

    @Test
    void exception_non_geree_devient_500_problem_json_sans_fuite_de_details() throws Exception {
        final String body = mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn().getResponse().getContentAsString();

        assertThat(body)
                .contains("\"title\":\"Erreur interne\"")
                .doesNotContain("détail interne sensible");
    }
}
