package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.buildingblocks.exception.ModuleValidationException;
import com.urgencesante.buildingblocks.security.TokenHasher;
import com.urgencesante.identity.IdentityFacade;
import com.urgencesante.identity.NewPortalCredential;
import com.urgencesante.identity.PortalRole;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Sécurité de la mise à jour de disponibilité sur base réelle : 401 sans
 * jeton, 403 hors périmètre, 200 avec un jeton opérateur du bon établissement.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PortalSecurityIntegrationTest extends AbstractPostgisIntegrationTest {

    private static final String FACILITY = "aaaaaaaa-0000-0000-0000-000000000001";
    private static final String OTHER = "aaaaaaaa-0000-0000-0000-000000000002";
    private static final String TOKEN = "operator-facility-1-token";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private IdentityFacade identityFacade;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM portal_credential WHERE label = 'IT' OR label LIKE 'IT-PROVISION%'");
        jdbc.update("DELETE FROM facility_service WHERE facility_id = ?::uuid", FACILITY);
        jdbc.update("DELETE FROM facility WHERE id = ?::uuid", FACILITY);
        jdbc.update(
                "INSERT INTO facility (id, name, location) VALUES "
                        + "(?::uuid, 'IT Facility', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography)",
                FACILITY);
        jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?::uuid, 'maternity')",
                FACILITY);
        jdbc.update(
                "INSERT INTO portal_credential (id, label, token_hash, role, facility_id, active) "
                        + "VALUES (gen_random_uuid(), 'IT', ?, 'FACILITY_OPERATOR', ?::uuid, TRUE)",
                TokenHasher.sha256Hex(TOKEN), FACILITY);
    }

    private ResponseEntity<String> put(String facility, String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange(
                "/api/v1/facilities/" + facility + "/availability/maternity",
                HttpMethod.PUT,
                new HttpEntity<>("{\"status\":\"AVAILABLE\"}", headers),
                String.class);
    }

    @Test
    void sans_jeton_401() {
        assertThat(put(FACILITY, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void hors_perimetre_403() {
        assertThat(put(OTHER, TOKEN).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void operateur_du_bon_etablissement_200() {
        assertThat(put(FACILITY, TOKEN).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── Provisioning bout-en-bout (issue #164) ──────────────────────────────
    // Preuve sur base réelle que la chaîne complète fonctionne : provisionner
    // via IdentityFacade (le même chemin que le CLI ProvisionPortalCredentialRunner)
    // → seule l'empreinte SHA-256 est en base, jamais le jeton en clair → ce
    // jeton authentifie réellement contre PortalSecurityInterceptor.

    @Test
    void un_credential_admin_provisionne_est_hache_en_base_et_authentifie_sur_tout_etablissement() {
        final NewPortalCredential provisioned =
                identityFacade.provision("IT-PROVISION Admin", PortalRole.ADMIN, null);

        final String storedHash = jdbc.queryForObject(
                "SELECT token_hash FROM portal_credential WHERE id = ?::uuid",
                String.class, provisioned.id().toString());
        assertThat(storedHash).isEqualTo(TokenHasher.sha256Hex(provisioned.rawToken()));
        assertThat(storedHash).isNotEqualTo(provisioned.rawToken());

        // Un ADMIN agit sur n'importe quel établissement — le même jeton
        // fraîchement provisionné doit donc autoriser ce PUT.
        assertThat(put(FACILITY, provisioned.rawToken()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void un_credential_operateur_provisionne_n_agit_que_sur_son_etablissement() {
        final NewPortalCredential provisioned =
                identityFacade.provision("IT-PROVISION Opérateur", PortalRole.FACILITY_OPERATOR, UUID.fromString(FACILITY));

        assertThat(put(FACILITY, provisioned.rawToken()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put(OTHER, provisioned.rawToken()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void refuse_de_provisionner_un_operateur_sans_etablissement() {
        assertThatThrownBy(() ->
                identityFacade.provision("IT-PROVISION Invalide", PortalRole.FACILITY_OPERATOR, null))
                .isInstanceOf(ModuleValidationException.class);

        final Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM portal_credential WHERE label = 'IT-PROVISION Invalide'", Integer.class);
        assertThat(count).isZero();
    }
}
