package com.urgencesante.availability.internal.adapter.out.event;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.port.out.AvailabilityEventPublisher;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import com.urgencesante.availability.internal.application.port.out.TransactionPort;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Relais de l'outbox : publie les événements en attente puis les marque
 * publiés. Un échec laisse l'événement en attente (attempts++) : REPRISE au
 * prochain passage.
 *
 * <p>Garantie : livraison AU MOINS UNE FOIS. Si une panne survient entre la
 * publication et le marquage, l'événement est republié — un {@code eventId}
 * stable est fourni pour permettre la déduplication. La consommation
 * idempotente (table de déduplication côté consommateur) reste À FOURNIR :
 * aucun consommateur idempotent n'est livré ici. Dette tracée.
 *
 * <p>Multi-instances (issue #134) : {@code unpublished()} pose un verrou
 * pessimiste sur les lignes lues (repository), et chaque passage est
 * transactionnel de bout en bout — le verrou reste posé pendant TOUTE la
 * publication et le marquage, jamais relâché entre-temps. Deux instances
 * concurrentes ne peuvent donc plus lire (et republier) les mêmes lignes.
 *
 * <p>Frontière transactionnelle PROGRAMMATIQUE ({@link TransactionPort}) et non
 * {@code @Transactional} : l'ordonnanceur appelle {@link #relay()}, qui
 * appelle {@link #relayOnce()} sur {@code this} — un appel interne qui
 * contourne le proxy Spring. Un {@code @Transactional} posé sur relayOnce()
 * était donc ignoré sur ce chemin : la lecture verrouillante échouait toutes
 * les 2 s (« Query requires transaction be in progress ») et aucun événement
 * n'était relayé. La frontière explicite vaut quel que soit l'appelant.
 */
@Component
public class OutboxRelay {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 50;

    private final OutboxPort outboxPort;
    private final AvailabilityEventPublisher publisher;
    private final TransactionPort transactionPort;

    public OutboxRelay(
            OutboxPort outboxPort,
            AvailabilityEventPublisher publisher,
            TransactionPort transactionPort) {
        this.outboxPort = Objects.requireNonNull(outboxPort);
        this.publisher = Objects.requireNonNull(publisher);
        this.transactionPort = Objects.requireNonNull(transactionPort);
    }

    @Scheduled(fixedDelayString = "${availability.outbox.relay-delay-ms:2000}")
    public void relay() {
        relayOnce();
    }

    /**
     * Un passage de relais, dans UNE transaction. Retourne le nombre publié.
     *
     * <p>Le verrou pessimiste posé par {@code unpublished()} reste tenu jusqu'au
     * commit, après publication et marquage de tout le lot (issue #134). Une
     * exception d'un événement individuel est interceptée localement (voir
     * {@code catch} ci-dessous) et ne fait donc pas échouer (ni rollback) le
     * traitement des autres événements du même lot.
     */
    public int relayOnce() {
        return transactionPort.inTransaction(this::relayPendingBatch);
    }

    private int relayPendingBatch() {
        final List<AvailabilityUpdated> pending = outboxPort.unpublished(BATCH_SIZE);
        int published = 0;
        for (final AvailabilityUpdated event : pending) {
            try {
                publisher.publish(event);
                outboxPort.markPublished(event.eventId());
                published++;
            } catch (RuntimeException exception) {
                outboxPort.recordFailure(event.eventId());
                LOG.warn("Publication différée de l'événement {} (reprise au prochain passage) : {}",
                        event.eventId(), exception.getMessage());
            }
        }
        return published;
    }
}
