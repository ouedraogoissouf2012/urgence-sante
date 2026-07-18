package com.urgencesante;

import java.util.Arrays;
import java.util.List;

/**
 * Politique CORS de l'application : origines autorisées, validées selon
 * l'environnement. Classe pure (sans Spring) pour être testable directement.
 */
public final class CorsPolicy {

    private final List<String> allowedOriginPatterns;

    private CorsPolicy(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = List.copyOf(allowedOriginPatterns);
    }

    /**
     * Construit la politique depuis la valeur configurée (liste séparée par
     * des virgules, vide = aucun accès cross-origin).
     *
     * @throws IllegalStateException en production si un motif générique
     *     (contenant «&nbsp;*&nbsp;») est configuré
     */
    public static CorsPolicy of(String rawPatterns, boolean productionProfile) {
        final List<String> patterns = rawPatterns == null || rawPatterns.isBlank()
                ? List.of()
                : Arrays.stream(rawPatterns.split(","))
                        .map(String::trim)
                        .filter(pattern -> !pattern.isEmpty())
                        .toList();

        if (productionProfile) {
            final List<String> generic = patterns.stream()
                    .filter(pattern -> pattern.contains("*"))
                    .toList();
            if (!generic.isEmpty()) {
                throw new IllegalStateException(
                        "Origines CORS génériques interdites en production : " + generic);
            }
        }
        return new CorsPolicy(patterns);
    }

    public List<String> allowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public boolean isEnabled() {
        return !allowedOriginPatterns.isEmpty();
    }
}
