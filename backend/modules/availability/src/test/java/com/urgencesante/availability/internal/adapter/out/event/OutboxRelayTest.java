package com.urgencesante.availability.internal.adapter.out.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import com.urgencesante.availability.internal.application.port.out.TransactionPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class OutboxRelayTest {

    /** Faux outbox en mémoire avec marquage et compteur d'échecs. */
    private static final class FakeOutbox implements OutboxPort {
        final Map<UUID, AvailabilityUpdated> pending = new LinkedHashMap<>();
        final Map<UUID, Integer> failures = new LinkedHashMap<>();
        final List<UUID> published = new ArrayList<>();
        /** Lectures verrouillantes faites HORS transaction (doit rester 0). */
        int lockingReadsOutsideTransaction = 0;
        BooleanSupplier transactionActive = () -> false;

        void add(AvailabilityUpdated event) {
            pending.put(event.eventId(), event);
        }

        @Override
        public void append(AvailabilityUpdated event) {
            add(event);
        }

        @Override
        public List<AvailabilityUpdated> unpublished(int limit) {
            // L'adaptateur réel pose FOR UPDATE SKIP LOCKED : sans transaction,
            // PostgreSQL/JPA refusent la requête (défaut de bascule production).
            if (!transactionActive.getAsBoolean()) {
                lockingReadsOutsideTransaction++;
            }
            return pending.values().stream().limit(limit).toList();
        }

        @Override
        public void markPublished(UUID eventId) {
            if (pending.remove(eventId) != null) {
                published.add(eventId);
            }
        }

        @Override
        public void recordFailure(UUID eventId) {
            failures.merge(eventId, 1, Integer::sum);
        }
    }

    private static AvailabilityUpdated event() {
        final UUID id = UUID.randomUUID();
        return new AvailabilityUpdated(id, id.toString(), UUID.randomUUID(),
                "maternity", "AVAILABLE",
                Instant.parse("2026-01-01T12:00:00Z"), Instant.parse("2026-01-01T12:00:00Z"));
    }

    private final FakeOutbox outbox = new FakeOutbox();
    private final List<AvailabilityUpdated> receivedEvents = new ArrayList<>();
    private boolean publisherDown = false;

    /** Fausse frontière transactionnelle : trace les transactions ouvertes. */
    private boolean transactionActive = false;
    private int transactionsOpened = 0;
    private final TransactionPort transactions = new TransactionPort() {
        @Override
        public <T> T inTransaction(Supplier<T> work) {
            transactionsOpened++;
            transactionActive = true;
            try {
                return work.get();
            } finally {
                transactionActive = false;
            }
        }
    };

    {
        outbox.transactionActive = () -> transactionActive;
    }

    private final OutboxRelay relay = new OutboxRelay(outbox, published -> {
        if (publisherDown) {
            throw new IllegalStateException("bus indisponible");
        }
        receivedEvents.add(published);
    }, transactions);

    @Test
    void publie_puis_marque_chaque_evenement_une_seule_fois() {
        final AvailabilityUpdated first = event();
        outbox.add(first);
        outbox.add(event());

        final int published = relay.relayOnce();
        final int secondPass = relay.relayOnce();

        assertThat(published).isEqualTo(2);
        assertThat(secondPass).isZero();
        assertThat(receivedEvents).hasSize(2);
        assertThat(outbox.published).hasSize(2).contains(first.eventId());
    }

    @Test
    void une_panne_de_publication_ne_perd_pas_l_evenement_et_reprend() {
        final AvailabilityUpdated pending = event();
        outbox.add(pending);
        publisherDown = true;

        assertThat(relay.relayOnce()).isZero();
        assertThat(outbox.pending).containsKey(pending.eventId());
        assertThat(outbox.failures.get(pending.eventId())).isEqualTo(1);

        publisherDown = false;
        assertThat(relay.relayOnce()).isEqualTo(1);
        assertThat(receivedEvents).extracting(AvailabilityUpdated::eventId)
                .containsExactly(pending.eventId());
    }

    @Test
    void le_point_d_entree_planifie_lit_l_outbox_dans_une_transaction() {
        // relay() est appelé par l'ordonnanceur : un @Transactional posé sur
        // relayOnce() était contourné par cet appel interne (pas de proxy).
        outbox.add(event());

        relay.relay();

        assertThat(transactionsOpened).as("une transaction par passage").isEqualTo(1);
        assertThat(outbox.lockingReadsOutsideTransaction).isZero();
        assertThat(receivedEvents).hasSize(1);
    }

    @Test
    void relayOnce_lit_aussi_l_outbox_dans_une_transaction() {
        outbox.add(event());

        relay.relayOnce();

        assertThat(transactionsOpened).isEqualTo(1);
        assertThat(outbox.lockingReadsOutsideTransaction).isZero();
    }

    @Test
    void un_rejeu_du_marquage_ne_cree_pas_de_doublon() {
        final AvailabilityUpdated only = event();
        outbox.add(only);
        relay.relayOnce();

        outbox.markPublished(only.eventId());

        assertThat(outbox.published).containsExactly(only.eventId());
    }
}
