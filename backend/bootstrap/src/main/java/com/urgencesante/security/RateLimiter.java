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
 * <p>Dette tracée : implémentation EN MÉMOIRE (une instance). Un déploiement
 * multi-instances nécessitera un limiteur partagé (ex. Redis) — hors périmètre
 * du MVP mono-instance.
 */
public final class RateLimiter {

    private final int capacity;
    private final double refillPerMilli;
    private final Clock clock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int capacity, Duration refillPeriod, Clock clock) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity doit être >= 1");
        }
        this.capacity = capacity;
        this.refillPerMilli = (double) capacity / refillPeriod.toMillis();
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        final long now = clock.millis();
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

    private static final class Bucket {
        private double tokens;
        private long lastRefillMillis;

        private Bucket(double tokens, long lastRefillMillis) {
            this.tokens = tokens;
            this.lastRefillMillis = lastRefillMillis;
        }
    }
}
