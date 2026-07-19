package com.urgencesante.routing.internal.adapter.out.osrm;

import com.urgencesante.routing.internal.domain.resilience.CircuitBreaker;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

/**
 * Santé du fournisseur d'itinéraires, dérivée du disjoncteur.
 *
 * <p>Circuit OUVERT → statut {@code DEGRADED} : l'application reste PRÊTE
 * (l'orientation fonctionne en mode dégradé, temps estimés) — cet indicateur
 * n'appartient donc PAS au groupe readiness, il signale l'état aux opérateurs.
 */
@Component("osrm")
public class OsrmHealthIndicator implements HealthIndicator {

    /** Statut dédié : service extérieur en panne, application utilisable. */
    public static final Status DEGRADED = new Status("DEGRADED",
            "fournisseur d'itinéraires indisponible, mode dégradé actif");

    private final CircuitBreaker circuitBreaker;

    public OsrmHealthIndicator(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Health health() {
        final CircuitBreaker.State state = circuitBreaker.state();
        return switch (state) {
            case CLOSED -> Health.up().withDetail("circuit", state.name()).build();
            case HALF_OPEN, OPEN ->
                    Health.status(DEGRADED).withDetail("circuit", state.name()).build();
        };
    }
}
