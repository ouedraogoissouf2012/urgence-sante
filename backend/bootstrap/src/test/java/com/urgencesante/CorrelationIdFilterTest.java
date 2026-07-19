package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    private String runAndCaptureMdc(MockHttpServletRequest request, MockHttpServletResponse response)
            throws ServletException, IOException {
        final AtomicReference<String> seen = new AtomicReference<>();
        filter.doFilter(request, response,
                new MockFilterChain() {
                    @Override
                    public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                        seen.set(MDC.get(CorrelationIdFilter.MDC_KEY));
                    }
                });
        return seen.get();
    }

    @Test
    void genere_un_identifiant_et_le_renvoie_dans_la_reponse() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final String inRequest = runAndCaptureMdc(new MockHttpServletRequest(), response);

        assertThat(inRequest).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo(inRequest);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).as("MDC nettoyé après la requête").isNull();
    }

    @Test
    void reutilise_un_identifiant_client_valide() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "portail-2026.07.19-abc");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final String inRequest = runAndCaptureMdc(request, response);

        assertThat(inRequest).isEqualTo("portail-2026.07.19-abc");
    }

    @Test
    void rejette_un_identifiant_client_malforme_et_en_genere_un() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "évil header\ninjection");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final String inRequest = runAndCaptureMdc(request, response);

        assertThat(inRequest).isNotEqualTo("évil header\ninjection").isNotBlank();
    }
}
