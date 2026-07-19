package com.urgencesante.facility.internal.domain.directory;

import java.util.List;

/**
 * Rapport d'un import : compteurs et détail des rejets (pour revue manuelle).
 *
 * @param rejected enregistrements refusés avec leurs motifs
 */
public record ImportReport(int inserted, int updated, List<Rejection> rejected) {

    public ImportReport {
        rejected = List.copyOf(rejected);
    }

    public int rejectedCount() {
        return rejected.size();
    }

    public int total() {
        return inserted + updated + rejected.size();
    }

    /** Enregistrement rejeté et ses motifs (référence pour traçabilité). */
    public record Rejection(String source, String externalRef, String name, List<String> reasons) {
        public Rejection {
            reasons = List.copyOf(reasons);
        }
    }
}
