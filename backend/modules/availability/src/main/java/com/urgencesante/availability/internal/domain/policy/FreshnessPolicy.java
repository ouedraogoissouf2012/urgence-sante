package com.urgencesante.availability.internal.domain.policy;

import com.urgencesante.availability.internal.domain.model.Freshness;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Détermine la fraîcheur d'une information selon son ancienneté.
 *
 * <p>Politique testable : la comparaison se fait entre un horodatage de mise à
 * jour et un instant « maintenant » fourni par l'appelant (horloge injectable).
 */
public final class FreshnessPolicy {

    private final Duration freshWithin;
    private final Duration agingWithin;

    public FreshnessPolicy(Duration freshWithin, Duration agingWithin) {
        this.freshWithin = Objects.requireNonNull(freshWithin, "freshWithin requis");
        this.agingWithin = Objects.requireNonNull(agingWithin, "agingWithin requis");
        if (freshWithin.isNegative() || freshWithin.isZero()) {
            throw new IllegalArgumentException("freshWithin doit être strictement positif");
        }
        if (agingWithin.compareTo(freshWithin) <= 0) {
            throw new IllegalArgumentException("agingWithin doit être supérieur à freshWithin");
        }
    }

    /** Politique par défaut : frais < 15 min, vieillissant < 60 min, sinon périmé. */
    public static FreshnessPolicy defaults() {
        return new FreshnessPolicy(Duration.ofMinutes(15), Duration.ofMinutes(60));
    }

    public Freshness evaluate(Instant updatedAt, Instant now) {
        Duration age = Duration.between(updatedAt, now);
        if (age.isNegative()) {
            age = Duration.ZERO;
        }
        if (age.compareTo(freshWithin) <= 0) {
            return Freshness.FRESH;
        }
        if (age.compareTo(agingWithin) <= 0) {
            return Freshness.AGING;
        }
        return Freshness.STALE;
    }
}
