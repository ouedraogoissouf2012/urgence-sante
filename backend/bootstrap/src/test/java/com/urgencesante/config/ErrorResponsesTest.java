package com.urgencesante.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Vérifie la garantie centrale de l'issue #148 : {@link ErrorResponses#toJson}
 * (utilisé par les intercepteurs, hors pipeline Spring MVC) produit EXACTEMENT
 * le même contrat JSON RFC 9457 que ce qu'un {@code @ExceptionHandler} standard
 * fait sérialiser par Spring MVC. Sans ce test, rien ne prouve que les deux
 * chemins convergent réellement vers le même format.
 */
class ErrorResponsesTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
    private final ErrorResponses errorResponses = new ErrorResponses(objectMapper);

    @RestController
    static class DummyController {
        @GetMapping("/boom")
        void boom() {
            throw new IllegalStateException("échec attendu");
        }
    }

    @RestControllerAdvice(assignableTypes = DummyController.class)
    static class DummyExceptionHandler {
        @ExceptionHandler(IllegalStateException.class)
        public ProblemDetail handle(IllegalStateException exception) {
            final ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.TOO_MANY_REQUESTS, "Trop de recherches, réessayez plus tard.");
            problem.setTitle("Trop de requêtes");
            return problem;
        }
    }

    @Test
    void writeProblem_produit_le_meme_contrat_json_qu_un_exception_handler_spring() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new DummyExceptionHandler())
                .build();

        final String fromExceptionHandler = mockMvc.perform(get("/boom"))
                .andExpect(status().isTooManyRequests())
                .andReturn().getResponse().getContentAsString();

        // Chemin réellement emprunté par les intercepteurs (writeProblem),
        // pas seulement toJson — pour que ce test couvre le vrai contrat de
        // bout en bout, "instance" (chemin de la requête) inclus.
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/boom");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        errorResponses.writeProblem(request, response, HttpStatus.TOO_MANY_REQUESTS,
                "Trop de requêtes", "Trop de recherches, réessayez plus tard.");

        final JsonNode expected = objectMapper.readTree(fromExceptionHandler);
        final JsonNode actual = objectMapper.readTree(response.getContentAsString());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writeProblem_ne_leve_pas_si_le_chemin_de_la_requete_n_est_pas_un_uri_valide() throws Exception {
        // request.getRequestURI() n'est pas garanti conforme à java.net.URI
        // (RFC 2396) du seul fait d'avoir traversé le conteneur servlet.
        // Avant correction, URI.create(...) levait ICI, AVANT que le statut
        // d'erreur ne soit écrit sur la réponse.
        final MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/v1/orientation/{bad}");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        errorResponses.writeProblem(request, response, HttpStatus.BAD_REQUEST, "Titre", "Détail");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        // RFC 9457 : "instance" est optionnel — repli sûr en son absence plutôt
        // que de casser toute la réponse d'erreur pour ce seul champ.
        assertThat(response.getContentAsString()).doesNotContain("\"instance\"");
    }

    @Test
    void writeProblem_force_l_encodage_utf_8() throws Exception {
        // response.getWriter() encode par défaut en ISO-8859-1 (défaut
        // servlet) sauf si l'encodage est explicitement forcé en UTF-8 —
        // sinon les accents seraient mal encodés ici mais corrects côté
        // MappingJackson2HttpMessageConverter (toujours UTF-8) : le contrat
        // ne serait alors PAS réellement unifié entre les deux chemins.
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/boom");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        errorResponses.writeProblem(request, response, HttpStatus.TOO_MANY_REQUESTS,
                "Trop de requêtes", "Trop de recherches, réessayez plus tard.");

        assertThat(response.getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");
        // getContentAsString() décoderait avec l'encodage COURANT de la même
        // réponse mock, donc round-trip toujours cohérent même sans le correctif
        // (test tautologique). On décode ici les octets bruts avec un charset
        // figé pour prouver que ce sont VRAIMENT des octets UTF-8.
        final byte[] rawBytes = response.getContentAsByteArray();
        assertThat(new String(rawBytes, StandardCharsets.UTF_8)).contains("réessayez");
        assertThat(new String(rawBytes, StandardCharsets.ISO_8859_1))
                .as("mêmes octets mal interprétés en ISO-8859-1 : ne doivent PAS correspondre")
                .doesNotContain("réessayez");
    }

    @Test
    void echappe_correctement_les_caracteres_speciaux_du_message() throws Exception {
        // Avant #148, PortalSecurityInterceptor composait le JSON avec
        // String.format sans échappement : un détail contenant des guillemets
        // aurait produit un JSON invalide. ErrorResponses délègue à Jackson,
        // qui échappe correctement.
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Message avec \"guillemets\" et \\ backslash");
        problem.setTitle("Titre");

        final String json = errorResponses.toJson(problem);
        final JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("detail").asText()).isEqualTo("Message avec \"guillemets\" et \\ backslash");
    }
}
