package com.urgencesante.security;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limiteur de débit par clé (seau à jetons), horloge injectable.
 *
 * <p>Chaque clé dispose de {@code capacity} jetons, reconstitués linéairement
 * sur {@code refillPeriod}. {@link #tryAcquire(String)} consomme un jeton si
 * disponible.
 *
 * <p>La table des seaux est BORNÉE : au-delà de {@link #maxBuckets} clés, une
 * purge opportuniste retire les seaux revenus à pleine capacité (donc inactifs :
 * les recréer à la demande donne le même état). Cela évite qu'un flux d'IP
 * sources distinctes ne fasse croître la table indéfiniment (fuite mémoire /
 * DoS mémoire lent) sur un endpoint public en écriture.
 *
 * <p>Dette tracée : implémentation EN MÉMOIRE (une instance). Un déploiement
 * multi-instances nécessitera un limiteur partagé (ex. Redis) — hors périmètre
 * du MVP mono-instance.
 */
public final class RateLimiter {

    /** Seuil de déclenchement de la purge des seaux inactifs. */
    private static final int DEFAULT_MAX_BUCKETS = 100_000;

    private final int capacity;
    private final double refillPerMilli;
    private final int maxBuckets;
    private final Clock clock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int capacity, Duration refillPeriod, Clock clock) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity doit être >= 1");
        }
        // Fail-fast : un refillPeriod nul rendrait refillPerMilli infini/NaN et
        // bloquerait SILENCIEUSEMENT tout le trafic (NaN >= 1.0 est faux). On
        // échoue au démarrage plutôt que de laisser une mauvaise config casser
        // le limiteur en production.
        if (refillPeriod == null || refillPeriod.toMillis() < 1) {
            throw new IllegalArgumentException("refillPeriod doit être >= 1 ms");
        }
        this.capacity = capacity;
        this.refillPerMilli = (double) capacity / refillPeriod.toMillis();
        this.maxBuckets = DEFAULT_MAX_BUCKETS;
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        final long now = clock.millis();
        if (buckets.size() >= maxBuckets) {
            evictReplenished(now);
        }
        final Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, now));
        synchronized (bucket) {
            final double refilled = (now - bucket.lastRefillMillis) * refillPerMilli;
            bucket.tokens = Math.min(capacity, bucket.tokens + refilled);
            bucket.lastRefillMillis = now;
            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    /**
     * Retire les seaux revenus à pleine capacité : inactifs, sans état à
     * préserver (les recréer à la demande donne exactement le même comportement).
     * Borne la mémoire sans jamais fausser la limite d'un client actif — un seau
     * partiellement consommé (donc en cours de limitation) n'est jamais évincé.
     */
    private void evictReplenished(long now) {
        buckets.forEach((key, bucket) -> {
            synchronized (bucket) {
                final double refilled = (now - bucket.lastRefillMillis) * refillPerMilli;
                if (bucket.tokens + refilled >= capacity) {
                    // remove(key, value) : ne supprime que si l'entrée n'a pas
                    // changé entre-temps (pas de course avec un computeIfAbsent).
                    buckets.remove(key, bucket);
                }
            }
        });
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillMillis;

        private Bucket(double tokens, long lastRefillMillis) {
            this.tokens = tokens;
            this.lastRefillMillis = lastRefillMillis;
        }
    }
}
