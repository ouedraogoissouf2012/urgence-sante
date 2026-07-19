package com.urgencesante.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.PortalRole;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PortalSecurityFilterTest {

    private static final UUID FACILITY = UUID.fromString("11111111-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("11111111-0000-0000-0000-000000000002");
    private static final String PATH = "/api/v1/facilities/" + FACILITY + "/availability/maternity";

    private IdentityFacade identity;
    private PortalSecurityFilter filter;

    @BeforeEach
    void setUp() {
        identity = rawToken -> switch (rawToken) {
            case "op-token" -> Optional.of(
                    new PortalPrincipalView("CHU", PortalRole.FACILITY_OPERATOR, FACILITY));
            case "admin-token" -> Optional.of(
                    new PortalPrincipalView("SAMU", PortalRole.ADMIN, null));
            default -> Optional.empty();
        };
        filter = newFilter(20, 60);
    }

    private PortalSecurityFilter newFilter(int authPerIp, int updates) {
        return new PortalSecurityFilter(
                identity,
                new RateLimiter(authPerIp, Duration.ofMinutes(1), Clock.systemUTC()),
                new RateLimiter(updates, Duration.ofMinutes(1), Clock.systemUTC()));
    }

    private MockHttpServletRequest put(String token) {
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT", PATH);
        request.setRequestURI(PATH);
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        return request;
    }

    private MockHttpServletResponse run(MockHttpServletRequest request, MockFilterChain chain)
            throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void laisse_passer_les_lectures_et_les_endpoints_publics() throws Exception {
        final MockHttpServletRequest get =
                new MockHttpServletRequest("GET", "/api/v1/orientation");
        get.setRequestURI("/api/v1/orientation");
        final MockFilterChain chain = new MockFilterChain();

        final MockHttpServletResponse response = run(get, chain);

        assertThat(chain.getRequest()).as("chaîne poursuivie").isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void refuse_401_sans_jeton_ou_jeton_invalide() throws Exception {
        assertThat(run(put(null), new MockFilterChain()).getStatus()).isEqualTo(401);
        assertThat(run(put("inconnu"), new MockFilterChain()).getStatus()).isEqualTo(401);
    }

    @Test
    void refuse_403_hors_perimetre() throws Exception {
        final String otherPath = "/api/v1/facilities/" + OTHER + "/availability/maternity";
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT", otherPath);
        request.setRequestURI(otherPath);
        request.addHeader("Authorization", "Bearer op-token");

        final MockHttpServletResponse response = run(request, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("problem+json");
    }

    @Test
    void autorise_l_operateur_sur_son_etablissement_et_l_admin_partout() throws Exception {
        final MockFilterChain opChain = new MockFilterChain();
        assertThat(run(put("op-token"), opChain).getStatus()).isEqualTo(200);
        assertThat(opChain.getRequest()).isNotNull();

        final MockFilterChain adminChain = new MockFilterChain();
        assertThat(run(put("admin-token"), adminChain).getStatus()).isEqualTo(200);
        assertThat(adminChain.getRequest()).isNotNull();
    }

    @Test
    void refuse_429_quand_le_debit_par_ip_est_depasse() throws Exception {
        filter = newFilter(1, 60); // 1 tentative/IP

        assertThat(run(put("op-token"), new MockFilterChain()).getStatus()).isEqualTo(200);
        assertThat(run(put("op-token"), new MockFilterChain()).getStatus()).isEqualTo(429);
    }

    @Test
    void ne_revele_jamais_le_jeton_dans_la_reponse_d_erreur() throws Exception {
        final MockHttpServletResponse response = run(put("inconnu"), new MockFilterChain());

        assertThat(response.getContentAsString()).doesNotContain("inconnu");
    }
}
