package com.urgencesante.orientation.internal.domain.model;

/**
 * Contribution d'une stratégie au classement d'un candidat : éligibilité
 * (une stratégie peut exclure un candidat), score et explication.
 */
public record ScoreContribution(boolean eligible, double score, String reason) {

    public static ScoreContribution eligible(double score, String reason) {
        return new ScoreContribution(true, score, reason);
    }

    public static ScoreContribution excluded(String reason) {
        return new ScoreContribution(false, 0.0, reason);
    }
}
