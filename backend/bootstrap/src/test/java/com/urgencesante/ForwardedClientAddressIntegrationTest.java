package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Issue #135 : derrière le reverse-proxy nginx du domaine (voir
 * deploy/setup-domaine.sh), {@code HttpServletRequest#getRemoteAddr()} doit
 * refléter l'IP RÉELLE du client (en-tête {@code X-Forwarded-For}), pas celle
 * du proxy — sinon tous les clients partagent un seul seau de débit
 * (throttling collatéral, ou anti-force-brute inopérant).
 *
 * <p>Le client de test se connecte en boucle locale (127.0.0.1), À
 * L'INTÉRIEUR de la liste blanche {@code internal-proxies} : il joue ici le
 * rôle du reverse-proxy de confiance, exactement comme nginx en production.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "security.orientation.rate.per-ip=1")
class ForwardedClientAddressIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    private ResponseEntity<String> orientation(String forwardedFor) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-For", forwardedFor);
        return rest.exchange(
                "/api/v1/orientation?lat=5.35&lon=-4.00&service=maternity",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Test
    void deux_ip_transmises_distinctes_ont_des_seaux_de_debit_independants() {
        assertThat(orientation("203.0.113.10").getStatusCode())
                .as("1re requête de la 1re IP transmise : autorisée")
                .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        assertThat(orientation("198.51.100.20").getStatusCode())
                .as("IP transmise DIFFÉRENTE : ne doit pas hériter du seau de la précédente — "
                        + "preuve que getRemoteAddr() reflète X-Forwarded-For, pas l'IP du proxy")
                .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        assertThat(orientation("203.0.113.10").getStatusCode())
                .as("rejeu de la 1re IP transmise : son seau (capacité 1) est déjà épuisé")
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
