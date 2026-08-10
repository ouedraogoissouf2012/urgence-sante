package com.urgencesante.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Limite de débit de l'orientation (issue #121) : endpoint public et anonyme
 * dont chaque appel peut retenir un thread servlet (OSRM en aval). Sans limite,
 * une IP peut à elle seule épuiser le pool de threads et rendre TOUTE l'API
 * indisponible — d'où ce garde-fou indépendant des autres limiteurs.
 */
class OrientationRateLimitInterceptorTest {

    private OrientationRateLimitInterceptor newInterceptor(int capacity) {
        return new OrientationRateLimitInterceptor(
                new RateLimiter(capacity, Duration.ofMinutes(1), Clock.systemUTC()));
    }

    private MockHttpServletRequest get(String remoteAddr) {
        final MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/v1/orientation");
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    @Test
    void autorise_jusqu_a_la_capacite_puis_refuse_avec_429() throws IOException {
        final OrientationRateLimitInterceptor interceptor = newInterceptor(3);

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
        assertThat(refused.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(refused.getContentAsString()).contains("429");
    }

    @Test
    void deux_ip_distinctes_ont_des_quotas_independants() throws IOException {
        final OrientationRateLimitInterceptor interceptor = newInterceptor(1);

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
}
