package com.urgencesante.availability.internal.application.port.out;

import com.urgencesante.availability.AvailabilityUpdated;
import java.util.List;
import java.util.UUID;

/**
 * Port sortant : outbox transactionnel des événements de disponibilité.
 * L'ajout se fait dans la transaction du cas d'usage ; le relais consomme
 * les événements en attente et marque la publication.
 */
public interface OutboxPort {

    /** Ajoute l'événement à l'outbox (même transaction que la persistance). */
    void append(AvailabilityUpdated event);

    /** Événements non publiés, du plus ancien au plus récent. */
    List<AvailabilityUpdated> unpublished(int limit);

    /** Marque l'événement publié (idempotent : un rejeu ne republie pas). */
    void markPublished(UUID eventId);

    /** Trace un échec de publication (l'événement reste en attente). */
    void recordFailure(UUID eventId);
}
