package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.patient.internal.adapter.out.session.PatientSessionPurgeJob;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Parcours de bout en bout des comptes patients sur PostGIS réel : inscription,
 * connexion, refus (mauvais mot de passe, doublon), purge des sessions
 * expirées, le tout via HTTP / persistance réelle.
 *
 * <p>Ignoré si Docker n'est pas joignable (voir {@link AbstractPostgisIntegrationTest}).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PatientApiIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PatientSessionPurgeJob purgeJob;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM patient_session");
        jdbc.update("DELETE FROM patient_account");
    }

    private Integer count(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    @Test
    void inscription_puis_connexion_reussissent_et_delivrent_un_jeton() {
        final ResponseEntity<Map> register = rest.postForEntity(
                "/api/v1/patients",
                Map.of("phone", "+225 0102030405", "password", "MotDePasse2026"),
                Map.class);

        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(register.getBody()).containsKeys("patientId", "token");
        assertThat((String) register.getBody().get("token")).isNotBlank();
        // Issue #130 : le compte ET sa première session existent après le SEUL
        // appel d'inscription — plus de fenêtre "compte sans session initiale".
        assertThat(count("patient_account")).isEqualTo(1);
        assertThat(count("patient_session")).isEqualTo(1);

        final ResponseEntity<Map> login = rest.postForEntity(
                "/api/v1/patients/session",
                Map.of("phone", "+2250102030405", "password", "MotDePasse2026"),
                Map.class);

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) login.getBody().get("token")).isNotBlank();
    }

    @Test
    void la_purge_supprime_les_sessions_expirees_et_conserve_les_valides() {
        final UUID patientId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO patient_account (id, phone, password_hash, active, created_at) "
                        + "VALUES (?, '+2250102030499', 'bcrypt$x', true, now())",
                patientId);
        final UUID expiredSession = UUID.randomUUID();
        final UUID validSession = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO patient_session (id, patient_id, token_hash, created_at, expires_at) "
                        + "VALUES (?, ?, 'hash-expiree', ?, ?)",
                expiredSession, patientId,
                Timestamp.from(Instant.now().minusSeconds(3600)), Timestamp.from(Instant.now().minusSeconds(1)));
        jdbc.update(
                "INSERT INTO patient_session (id, patient_id, token_hash, created_at, expires_at) "
                        + "VALUES (?, ?, 'hash-valide', ?, ?)",
                validSession, patientId,
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(3600)));

        final int purged = purgeJob.purgeOnce();

        assertThat(purged).isEqualTo(1);
        assertThat(count("patient_session")).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM patient_session WHERE id = ?", Integer.class, validSession))
                .isEqualTo(1);
    }

    @Test
    void le_mot_de_passe_n_est_pas_stocke_en_clair() {
        rest.postForEntity("/api/v1/patients",
                Map.of("phone", "+2250102030405", "password", "MotDePasse2026"), Map.class);

        final String hash = jdbc.queryForObject(
                "SELECT password_hash FROM patient_account WHERE phone = ?",
                String.class, "+2250102030405");
        assertThat(hash).doesNotContain("MotDePasse2026").startsWith("$2");
    }

    @Test
    void connexion_avec_mauvais_mot_de_passe_est_refusee_401() {
        rest.postForEntity("/api/v1/patients",
                Map.of("phone", "+2250102030405", "password", "MotDePasse2026"), Map.class);

        final ResponseEntity<Map> login = rest.postForEntity(
                "/api/v1/patients/session",
                Map.of("phone", "+2250102030405", "password", "Faux"),
                Map.class);

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void inscription_en_double_est_refusee_409() {
        rest.postForEntity("/api/v1/patients",
                Map.of("phone", "+2250102030405", "password", "MotDePasse2026"), Map.class);

        final ResponseEntity<Map> second = rest.postForEntity(
                "/api/v1/patients",
                Map.of("phone", "+225 0102030405", "password", "AutreMotDePasse"),
                Map.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mot_de_passe_trop_court_est_refuse_400() {
        final ResponseEntity<Map> register = rest.postForEntity(
                "/api/v1/patients",
                Map.of("phone", "+2250102030405", "password", "court"),
                Map.class);

        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
