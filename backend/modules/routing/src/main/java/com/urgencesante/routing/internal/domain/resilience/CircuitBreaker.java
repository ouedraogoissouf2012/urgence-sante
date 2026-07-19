package com.urgencesante.routing.internal.domain.resilience;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Disjoncteur simple pour un fournisseur externe. Java pur, horloge injectable.
 *
 * <p>États : FERMÉ (appels autorisés) → OUVERT après {@code failureThreshold}
 * échecs consécutifs (appels refusés immédiatement, aucune latence réseau) →
 * SEMI-OUVERT après {@code openDuration} (un appel d'essai : succès → FERMÉ,
 * échec → OUVERT à nouveau).
 *
 * <p>Synchronisé : l'état est partagé entre requêtes concurrentes.
 */
public final class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final Duration openDuration;
    private final Clock clock;

    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private Instant openedAt;
    /** Vrai quand un essai de reprise (semi-ouvert) est déjà en vol. */
    private boolean trialInFlight = false;

    public CircuitBreaker(int failureThreshold, Duration openDuration, Clock clock) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold doit être >= 1");
        }
        this.failureThreshold = failureThreshold;
        this.openDuration = Objects.requireNonNull(openDuration);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Vrai si un appel peut être tenté (FERMÉ, ou UNIQUE essai SEMI-OUVERT).
     *
     * <p>En semi-ouvert, un seul appel d'essai est autorisé à la fois : les
     * appels concurrents suivants sont refusés tant que l'essai n'a pas été
     * résolu par {@link #recordSuccess()} ou {@link #recordFailure()} — le
     * fournisseur en cours de reprise n'est jamais martelé.
     */
    public synchronized boolean allowRequest() {
        if (state == State.OPEN
                && Duration.between(openedAt, clock.instant()).compareTo(openDuration) >= 0) {
            state = State.HALF_OPEN;
            trialInFlight = false;
        }
        if (state == State.HALF_OPEN) {
            if (trialInFlight) {
                return false;
            }
            trialInFlight = true;
            return true;
        }
        return state == State.CLOSED;
    }

    /** Signale un succès : referme le circuit. */
    public synchronized void recordSuccess() {
        consecutiveFailures = 0;
        trialInFlight = false;
        state = State.CLOSED;
    }

    /** Signale un échec : ouvre le circuit au seuil, ou depuis SEMI-OUVERT. */
    public synchronized void recordFailure() {
        consecutiveFailures++;
        trialInFlight = false;
        if (state == State.HALF_OPEN || consecutiveFailures >= failureThreshold) {
            state = State.OPEN;
            openedAt = clock.instant();
        }
    }

    public synchronized State state() {
        return state;
    }
}
