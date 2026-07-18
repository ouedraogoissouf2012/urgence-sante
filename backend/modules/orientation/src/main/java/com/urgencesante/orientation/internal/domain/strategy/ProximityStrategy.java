package com.urgencesante.orientation.internal.domain.strategy;

import com.urgencesante.orientation.internal.domain.model.CandidateEvaluation;
import com.urgencesante.orientation.internal.domain.model.ScoreContribution;
import java.util.Locale;

/**
 * Classe selon la proximité : plus le temps de trajet est court, meilleur est le
 * score. En mode dégradé (temps de trajet indisponible), un temps est estimé à
 * partir de la distance à vol d'oiseau.
 */
public class ProximityStrategy implements OrientationStrategy {

    private static final double ASSUMED_SPEED_KMH = 40.0;
    private static final double MAX_SCORE = 60.0;

    @Override
    public ScoreContribution evaluate(CandidateEvaluation candidate) {
        final double km = candidate.distanceMeters() / 1000.0;
        final double minutes;
        final String reason;
        if (candidate.hasTravelTime()) {
            minutes = candidate.travelTimeSeconds() / 60.0;
            reason = String.format(Locale.ROOT, "à %.1f km (~%.0f min)", km, minutes);
        } else {
            minutes = km / ASSUMED_SPEED_KMH * 60.0;
            reason = String.format(Locale.ROOT, "à %.1f km (temps de trajet estimé)", km);
        }
        return ScoreContribution.eligible(Math.max(0.0, MAX_SCORE - minutes), reason);
    }
}
