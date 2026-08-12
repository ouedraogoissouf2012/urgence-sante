package com.urgencesante.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.config.ErrorResponses;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Limiteur de débit générique (issue #147) : remplace {@code
 * PatientRateLimitInterceptor} et {@code OrientationRateLimitInterceptor},
 * identiques à 95 % (seuls la clé et le message différaient). Ce test exerce
 * le comportement générique une fois — la paramétrisation (clé, message) est
 * elle-même triviale et couverte par {@link SecurityConfiguration}.
 */
class GenericRateLimitInterceptorTest {

    private static final ErrorResponses ERROR_RESPONSES =
            new ErrorResponses(Jackson2ObjectMapperBuilder.json().build());

    private GenericRateLimitInterceptor newInterceptor(int capacity) {
        return new GenericRateLimitInterceptor(
                new RateLimiter(capacity, Duration.ofMinutes(1), Clock.systemUTC()),
                "orientation-ip:",
                ERROR_RESPONSES,
                "Trop de recherches, réessayez plus tard.");
    }

    private MockHttpServletRequest get(String remoteAddr) {
        final MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/v1/orientation");
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    @Test
    void autorise_jusqu_a_la_capacite_puis_refuse_avec_429() throws IOException {
        final GenericRateLimitInterceptor interceptor = newInterceptor(3);

        for (int i = 0; i < 3; i++) {
            final MockHttpServletResponse response = new MockHttpServletResponse();
            assertThat(interceptor.preHandle(get("203.0.113.10"), response, new Object()))
                    .as("requête %d autorisée", i + 1)
                    .isTrue();
        }

        final MockHttpServletResponse refused = new MockHttpServletResponse();
        final boolean allowed = interceptor.preHandle(get("203.0.113.10"), refused, new Object());

        assertThat(allowed).as("4e requête refusée").isFalse();
        assertThat(refused.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        // charset=UTF-8 est explicitement ajouté (voir
        // les_accents_du_message_sont_ecrits_en_utf_8) : on vérifie ici le
        // seul type de média.
        assertThat(refused.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        // Corps ProblemDetail RFC 9457, sérialisé via ErrorResponses (issue #148) :
        // le message paramétré au constructeur se retrouve tel quel dans "detail",
        // et "instance" est peuplé comme le ferait Spring pour un @ExceptionHandler.
        assertThat(refused.getContentAsString())
                .contains("\"status\":429")
                .contains("\"title\":\"Trop de requêtes\"")
                .contains("\"detail\":\"Trop de recherches, réessayez plus tard.\"")
                .contains("\"instance\":\"/api/v1/orientation\"");
    }

    @Test
    void deux_ip_distinctes_ont_des_quotas_independants() throws IOException {
        final GenericRateLimitInterceptor interceptor = newInterceptor(1);

        assertThat(interceptor.preHandle(
                get("203.0.113.10"), new MockHttpServletResponse(), new Object()))
                .isTrue();
        // Une SECONDE ip n'est pas pénalisée par la première (pas de seau global).
        assertThat(interceptor.preHandle(
                get("198.51.100.20"), new MockHttpServletResponse(), new Object()))
                .as("IP différente, quota indépendant")
                .isTrue();
        // La première IP, elle, est bien épuisée.
        assertThat(interceptor.preHandle(
                get("203.0.113.10"), new MockHttpServletResponse(), new Object()))
                .isFalse();
    }

    @Test
    void la_cle_du_seau_est_prefixee_pour_isoler_les_deux_usages_du_limiteur() throws IOException {
        // Deux intercepteurs configurés avec des préfixes différents (comme
        // patient-ip: et orientation-ip: dans SecurityConfiguration) sur la
        // MÊME IP ne doivent PAS partager de seau : chacun a son propre
        // RateLimiter en pratique, mais même en cas de RateLimiter partagé par
        // erreur, un préfixe différent isole les clés — ce test verrouille ce
        // contrat de paramétrisation.
        final RateLimiter shared = new RateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        final GenericRateLimitInterceptor patientLike =
                new GenericRateLimitInterceptor(shared, "patient-ip:", ERROR_RESPONSES, "Trop de tentatives.");
        final GenericRateLimitInterceptor orientationLike =
                new GenericRateLimitInterceptor(shared, "orientation-ip:", ERROR_RESPONSES, "Trop de recherches.");

        assertThat(patientLike.preHandle(get("203.0.113.10"), new MockHttpServletResponse(), new Object()))
                .isTrue();
        assertThat(orientationLike.preHandle(get("203.0.113.10"), new MockHttpServletResponse(), new Object()))
                .as("préfixe différent => clé de seau différente => quota indépendant")
                .isTrue();
    }

    @Test
    void un_chemin_de_requete_non_conforme_a_uri_ne_fait_pas_planter_le_rejet() throws IOException {
        // Un chemin brut n'est pas garanti conforme à java.net.URI (RFC 2396)
        // par le seul fait d'avoir traversé le conteneur servlet. Avant
        // correction, URI.create(...) levait AVANT l'écriture du 429, ce qui
        // transformait le rejet voulu en exception non gérée.
        final GenericRateLimitInterceptor interceptor = newInterceptor(1);
        final MockHttpServletRequest firstRequest =
                new MockHttpServletRequest("GET", "/api/v1/orientation/{bad}");
        firstRequest.setRemoteAddr("203.0.113.10");
        interceptor.preHandle(firstRequest, new MockHttpServletResponse(), new Object());

        final MockHttpServletRequest secondRequest =
                new MockHttpServletRequest("GET", "/api/v1/orientation/{bad}");
        secondRequest.setRemoteAddr("203.0.113.10");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(secondRequest, response, new Object())).isFalse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getContentAsString()).doesNotContain("\"instance\"");
    }

    @Test
    void les_accents_du_message_sont_ecrits_en_utf_8() throws IOException {
        // response.getWriter() encode par défaut en ISO-8859-1 (défaut
        // servlet) sauf si l'encodage est explicitement forcé en UTF-8 — sans
        // quoi les accents du message ("réessayez") seraient mal encodés ici
        // alors qu'un @ExceptionHandler (MappingJackson2HttpMessageConverter,
        // toujours UTF-8) les rendrait correctement : contrat non unifié.
        final GenericRateLimitInterceptor interceptor = newInterceptor(1);
        interceptor.preHandle(get("203.0.113.10"), new MockHttpServletResponse(), new Object());
        final MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(get("203.0.113.10"), response, new Object());

        assertThat(response.getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");
        // getContentAsString() décoderait avec l'encodage COURANT de la même
        // réponse mock, donc round-trip toujours cohérent même sans le
        // correctif (test tautologique). On décode ici les octets bruts avec
        // un charset figé pour prouver que ce sont VRAIMENT des octets UTF-8.
        final byte[] rawBytes = response.getContentAsByteArray();
        assertThat(new String(rawBytes, StandardCharsets.UTF_8)).contains("réessayez");
        assertThat(new String(rawBytes, StandardCharsets.ISO_8859_1))
                .as("mêmes octets mal interprétés en ISO-8859-1 : ne doivent PAS correspondre")
                .doesNotContain("réessayez");
    }
}
