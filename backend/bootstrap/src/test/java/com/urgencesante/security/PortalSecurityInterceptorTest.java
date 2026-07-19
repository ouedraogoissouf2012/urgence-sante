package com.urgencesante.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.PortalRole;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

class PortalSecurityInterceptorTest {

    private static final UUID FACILITY = UUID.fromString("11111111-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("11111111-0000-0000-0000-000000000002");
    private static final UUID OP_ID = UUID.randomUUID();

    private IdentityFacade identity;
    private PortalSecurityInterceptor interceptor;

    @BeforeEach
    void setUp() {
        identity = rawToken -> switch (rawToken) {
            case "op-token" -> Optional.of(
                    new PortalPrincipalView(OP_ID, "CHU", PortalRole.FACILITY_OPERATOR, FACILITY));
            case "admin-token" -> Optional.of(
                    new PortalPrincipalView(UUID.randomUUID(), "SAMU", PortalRole.ADMIN, null));
            default -> Optional.empty();
        };
        interceptor = newInterceptor(20, 60);
    }

    private PortalSecurityInterceptor newInterceptor(int authPerIp, int updates) {
        return new PortalSecurityInterceptor(
                identity,
                new RateLimiter(authPerIp, Duration.ofMinutes(1), Clock.systemUTC()),
                new RateLimiter(updates, Duration.ofMinutes(1), Clock.systemUTC()));
    }

    /** PUT routé par Spring : variables de gabarit déjà décodées/normalisées. */
    private MockHttpServletRequest put(String token, UUID routedFacility) {
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT",
                "/api/v1/facilities/" + routedFacility + "/availability/maternity");
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                Map.of("facilityId", routedFacility.toString(), "serviceCode", "maternity"));
        return request;
    }

    private MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        // preHandle pose le statut d'erreur sur rejet ; sur succès (true), le
        // statut reste 200 par défaut.
        interceptor.preHandle(request, response, new Object());
        return response;
    }

    @Test
    void laisse_passer_les_lectures() throws Exception {
        final MockHttpServletRequest get =
                new MockHttpServletRequest("GET", "/api/v1/facilities/" + FACILITY + "/availability");
        assertThat(interceptor.preHandle(get, new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    void refuse_401_sans_jeton_ou_jeton_invalide() throws Exception {
        assertThat(run(put(null, FACILITY)).getStatus()).isEqualTo(401);
        assertThat(run(put("inconnu", FACILITY)).getStatus()).isEqualTo(401);
    }

    @Test
    void refuse_403_hors_perimetre() throws Exception {
        final MockHttpServletResponse response = run(put("op-token", OTHER));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("problem+json");
    }

    @Test
    void autorise_l_operateur_sur_son_etablissement_et_l_admin_partout() throws Exception {
        assertThat(interceptor.preHandle(
                put("op-token", FACILITY), new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(interceptor.preHandle(
                put("admin-token", OTHER), new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    void refuse_429_quand_le_debit_par_ip_est_depasse() throws Exception {
        interceptor = newInterceptor(1, 60);
        assertThat(run(put("op-token", FACILITY)).getStatus()).isEqualTo(200);
        assertThat(run(put("op-token", FACILITY)).getStatus()).isEqualTo(429);
    }

    @Test
    void echoue_ferme_si_l_identifiant_route_est_absent() throws Exception {
        // Chemin protégé atteint sans variable facilityId exploitable : REJET
        // (non « laissé passer »), sinon contournement d'auth.
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT",
                "/api/v1/facilities/x/availability/maternity");
        request.addHeader("Authorization", "Bearer op-token");
        // pas d'attribut URI_TEMPLATE_VARIABLES → identifiant introuvable
        final MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void l_ip_du_debit_vient_du_transport_pas_d_un_en_tete_falsifiable() throws Exception {
        interceptor = newInterceptor(1, 60);
        // Deux requêtes avec des X-Forwarded-For DIFFÉRENTS mais même transport :
        // la seconde doit être limitée (l'en-tête ne réinitialise pas le seau).
        final MockHttpServletRequest r1 = put("op-token", FACILITY);
        r1.addHeader("X-Forwarded-For", "1.1.1.1");
        final MockHttpServletRequest r2 = put("op-token", FACILITY);
        r2.addHeader("X-Forwarded-For", "2.2.2.2");

        assertThat(interceptor.preHandle(r1, new MockHttpServletResponse(), new Object())).isTrue();
        final MockHttpServletResponse blocked = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(r2, blocked, new Object())).isFalse();
        assertThat(blocked.getStatus()).isEqualTo(429);
    }
}
