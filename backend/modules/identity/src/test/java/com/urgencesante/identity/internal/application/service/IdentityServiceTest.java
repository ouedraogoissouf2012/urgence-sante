package com.urgencesante.identity.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.identity.PortalPrincipalView;
import com.urgencesante.identity.PortalRole;
import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.domain.model.PortalCredential;
import com.urgencesante.identity.internal.domain.model.TokenHasher;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityServiceTest {

    private static final UUID FACILITY = UUID.randomUUID();
    private static final String RAW = "operator-secret-token";

    /** Faux dépôt : ne connaît que l'empreinte du jeton RAW. */
    private final LoadCredentialPort loadPort = tokenHash ->
            tokenHash.equals(TokenHasher.sha256Hex(RAW))
                    ? Optional.of(new PortalCredential(
                            UUID.randomUUID(), "CHU Cocody", tokenHash,
                            PortalRole.FACILITY_OPERATOR, FACILITY, true))
                    : Optional.empty();

    private final IdentityService service = new IdentityService(loadPort);

    @Test
    void authentifie_un_jeton_valide_et_expose_la_portee() {
        final Optional<PortalPrincipalView> principal = service.authenticate(RAW);

        assertThat(principal).isPresent();
        assertThat(principal.get().role()).isEqualTo(PortalRole.FACILITY_OPERATOR);
        assertThat(principal.get().canActOn(FACILITY)).isTrue();
        assertThat(principal.get().canActOn(UUID.randomUUID())).isFalse();
    }

    @Test
    void rejette_un_jeton_inconnu_vide_ou_nul() {
        assertThat(service.authenticate("mauvais")).isEmpty();
        assertThat(service.authenticate("  ")).isEmpty();
        assertThat(service.authenticate(null)).isEmpty();
    }

    @Test
    void un_admin_agit_sur_tout_etablissement() {
        final PortalPrincipalView admin =
                new PortalPrincipalView(UUID.randomUUID(), "Régulation", PortalRole.ADMIN, null);

        assertThat(admin.canActOn(UUID.randomUUID())).isTrue();
        assertThat(admin.canActOn(FACILITY)).isTrue();
    }

    @Test
    void l_empreinte_est_stable_et_ne_revele_pas_le_jeton() {
        final String hash = TokenHasher.sha256Hex(RAW);

        assertThat(hash).hasSize(64).doesNotContain(RAW);
        assertThat(TokenHasher.sha256Hex(RAW)).isEqualTo(hash);
    }
}
