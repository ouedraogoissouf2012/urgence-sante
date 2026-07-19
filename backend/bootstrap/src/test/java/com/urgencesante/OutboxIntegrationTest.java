package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.availability.internal.adapter.out.event.OutboxRelay;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.in.UpdateAvailabilityUseCase;
import com.urgencesante.availability.internal.domain.exception.ServiceNotOfferedException;
import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Vérifie sur PostgreSQL réel l'atomicité courant + historique + outbox et la
 * publication par le relais (marquage, pas de doublon).
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxIntegrationTest extends AbstractPostgisIntegrationTest {

    private static final UUID FACILITY = UUID.fromString("eeeeeeee-0000-0000-0000-000000000001");

    @Autowired
    private UpdateAvailabilityUseCase updateAvailability;

    @Autowired
    private OutboxRelay relay;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM availability_outbox");
        jdbc.update("DELETE FROM availability_history");
        jdbc.update("DELETE FROM availability");
        jdbc.update("DELETE FROM facility_service");
        jdbc.update("DELETE FROM facility WHERE id = ?", FACILITY);
        jdbc.update(
                "INSERT INTO facility (id, name, location) "
                        + "VALUES (?, 'Test Outbox', ST_SetSRID(ST_MakePoint(-4.0, 5.35), 4326)::geography)",
                FACILITY);
        jdbc.update(
                "INSERT INTO facility_service (facility_id, service_code) VALUES (?, 'maternity')",
                FACILITY);
    }

    private Integer count(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    @Test
    void commit_atomique_puis_publication_marquee_sans_doublon() {
        updateAvailability.update(
                new UpdateAvailabilityCommand(FACILITY, "maternity", AvailabilityStatus.LIMITED));

        // Atomicité : les trois écritures sont présentes après commit.
        assertThat(count("availability")).isEqualTo(1);
        assertThat(count("availability_history")).isEqualTo(1);
        assertThat(count("availability_outbox")).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM availability_outbox WHERE published_at IS NULL", Integer.class))
                .isEqualTo(1);

        // Relais : publie et marque ; un second passage ne republie rien.
        assertThat(relay.relayOnce()).isEqualTo(1);
        assertThat(relay.relayOnce()).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM availability_outbox WHERE published_at IS NOT NULL", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void un_refus_metier_n_ecrit_rien_du_tout() {
        assertThatThrownBy(() -> updateAvailability.update(
                new UpdateAvailabilityCommand(FACILITY, "surgery", AvailabilityStatus.LIMITED)))
                .isInstanceOf(ServiceNotOfferedException.class);

        assertThat(count("availability")).isZero();
        assertThat(count("availability_history")).isZero();
        assertThat(count("availability_outbox")).isZero();
    }
}
