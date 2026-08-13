package com.urgencesante.audit.internal.adapter.in.event;

import com.urgencesante.audit.internal.application.command.RecordAuditEntryCommand;
import com.urgencesante.audit.internal.application.port.in.RecordAuditEntryUseCase;
import com.urgencesante.availability.AvailabilityUpdated;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Écouteur d'audit pour {@link AvailabilityUpdated}, publié par le relais
 * outbox du module Availability ({@code OutboxRelay}).
 *
 * <p>{@code AFTER_COMMIT} : n'enregistre la ligne d'audit qu'une fois le
 * passage de relais durablement validé (marquage {@code published_at}
 * inclus) — un relais qui échoue et rejoue plus tard ne produit donc jamais
 * une trace pour un événement qui, finalement, n'aurait pas été publié.
 *
 * <p>Une exception de {@link RecordAuditEntryUseCase} est interceptée et
 * journalisée ici : à ce stade la transaction source est déjà validée, la
 * propager ne annulerait rien et ferait seulement du bruit dans le relais
 * planifié. C'est une perte silencieuse assumée (dette tracée, voir ADR-006)
 * — pas de nouvelle tentative automatique pour cet événement précis ; le
 * compteur {@code audit.record.errors} (même usage que
 * {@code routing.OsrmRouteProvider.errorCounter}) reste le seul filet
 * d'observation tant qu'un vrai mécanisme de reprise n'existe pas.
 */
@Component
public class AvailabilityUpdatedAuditListener {

    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityUpdatedAuditListener.class);
    private static final String ACTION = "AVAILABILITY_UPDATED";
    private static final String RESOURCE_TYPE = "FACILITY_SERVICE_AVAILABILITY";

    private final RecordAuditEntryUseCase recordAuditEntry;
    private final Counter recordErrorCounter;

    public AvailabilityUpdatedAuditListener(RecordAuditEntryUseCase recordAuditEntry, MeterRegistry meterRegistry) {
        this.recordAuditEntry = recordAuditEntry;
        this.recordErrorCounter = meterRegistry.counter("audit.record.errors");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAvailabilityUpdated(AvailabilityUpdated event) {
        try {
            recordAuditEntry.record(new RecordAuditEntryCommand(
                    event.eventId(),
                    ACTION,
                    RESOURCE_TYPE,
                    resourceId(event),
                    // Acteur non résolu ici : PortalSecurityInterceptor (bootstrap)
                    // authentifie l'agent mais ne le propage pas jusqu'à l'événement.
                    null,
                    null,
                    event.correlationId(),
                    event.occurredAt()));
        } catch (RuntimeException recordingFailed) {
            recordErrorCounter.increment();
            LOG.error(
                    "Échec d'enregistrement de l'audit pour l'événement {} (corrélation {}) — "
                            + "ligne perdue, aucune nouvelle tentative automatique.",
                    event.eventId(), event.correlationId(), recordingFailed);
        }
    }

    private static String resourceId(AvailabilityUpdated event) {
        return event.facilityId() + ":" + event.serviceCode();
    }
}
