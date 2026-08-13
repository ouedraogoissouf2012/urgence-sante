package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.availability.internal.adapter.out.event.OutboxRelay;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.in.UpdateAvailabilityUseCase;
import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Preuve bout-en-bout (issue #136) : une vraie mise à jour de disponibilité,
 * relayée par l'outbox réel, produit une ligne d'audit réellement persistée —
 * sans mocker le consommateur ({@code AvailabilityUpdatedAuditListener}) ni
 * l'adaptateur de persistance.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuditIntegrationTest extends AbstractPostgisIntegrationTest {

    private static final UUID FACILITY = UUID.fromString("eeeeeeee-0000-0000-0000-000000000002");

    @Autowired
    private UpdateAvailabilityUseCase updateAvailability;

    @Autowired
    private OutboxRelay relay;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM audit_entry");
        jdbc.update("DELETE FROM availability_outbox");
        jdbc.update("DELETE FROM availability_history");
        jdbc.update("DELETE FROM availability");
        jdbc.update("DELETE FROM facility_service");
        jdbc.update("DELETE FROM facility WHERE id = ?", FACILITY);
        jdbc.update(
                "INSERT INTO facility (id, name, location) "
                        + "VALUES (?, 'Test Audit', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography)",
                FACILITY);
        jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?, 'maternity')",
                FACILITY);
    }

    @Test
    void une_mise_a_jour_de_disponibilite_relayee_produit_une_ligne_d_audit() {
        updateAvailability.update(
                new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED));

        assertThat(relay.relayOnce()).isEqualTo(1);

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT action, resource_type, resource_id, actor_id, correlation_id FROM audit_entry");
        assertThat(rows).hasSize(1);
        final Map<String, Object> row = rows.get(0);
        assertThat(row.get("action")).isEqualTo("AVAILABILITY_UPDATED");
        assertThat(row.get("resource_type")).isEqualTo("FACILITY_SERVICE_AVAILABILITY");
        assertThat(row.get("resource_id")).isEqualTo(FACILITY + ":maternity");
        // Dette connue (ADR-006) : l'acteur n'est pas encore propagé jusqu'à l'événement.
        assertThat(row.get("actor_id")).isNull();
    }

    @Test
    void une_redelivraison_du_meme_evenement_ne_duplique_pas_la_ligne_d_audit() {
        updateAvailability.update(
                new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED));
        relay.relayOnce();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_entry", Integer.class)).isEqualTo(1);

        // Simule une redélivraison outbox (livraison au moins une fois) : l'événement
        // redevient "en attente" côté source, sans qu'aucun nouveau fait métier n'existe.
        jdbc.update("UPDATE availability_outbox SET published_at = NULL");
        assertThat(relay.relayOnce()).isEqualTo(1); // republié avec succès côté outbox...

        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_entry", Integer.class))
                .isEqualTo(1); // ...mais la ligne d'audit reste unique (idempotence).
    }
}
