package com.urgencesante.availability.internal.adapter.out.event;

import com.urgencesante.availability.AvailabilityUpdated;
import com.urgencesante.availability.internal.application.port.out.AvailabilityEventPublisher;
import com.urgencesante.availability.internal.application.port.out.OutboxPort;
import java.util.List;
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
 * <p>Ordonnancement mono-instance : ce relais suppose une seule instance
 * active. En multi-instances, il faudrait un verrou (ex.
 * {@code FOR UPDATE SKIP LOCKED}) pour éviter les doubles publications
 * concurrentes — hors périmètre du MVP mono-instance.
 */
@Component
public class OutboxRelay {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 50;

    private final OutboxPort outboxPort;
    private final AvailabilityEventPublisher publisher;

    public OutboxRelay(OutboxPort outboxPort, AvailabilityEventPublisher publisher) {
        this.outboxPort = outboxPort;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${availability.outbox.relay-delay-ms:2000}")
    public void relay() {
        relayOnce();
    }

    /** Un passage de relais (extrait pour les tests). Retourne le nombre publié. */
    public int relayOnce() {
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
