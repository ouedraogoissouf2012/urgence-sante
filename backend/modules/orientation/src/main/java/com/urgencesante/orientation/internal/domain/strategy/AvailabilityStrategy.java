package com.urgencesante.orientation.internal.domain.strategy;

import com.urgencesante.orientation.internal.domain.model.CandidateEvaluation;
import com.urgencesante.orientation.internal.domain.model.ScoreContribution;

/**
 * Classe selon le statut de disponibilité. Un établissement fermé est exclu ;
 * un statut non confirmé (dont un statut périmé, déjà ramené à « UNKNOWN » par
 * le moteur) reste éligible mais moins favorisé.
 */
public class AvailabilityStrategy implements OrientationStrategy {

    @Override
    public ScoreContribution evaluate(CandidateEvaluation candidate) {
        return switch (candidate.status()) {
            case "AVAILABLE" -> ScoreContribution.eligible(100.0, "service disponible");
            case "LIMITED" -> ScoreContribution.eligible(60.0, "disponibilité limitée");
            case "SATURATED" -> ScoreContribution.eligible(20.0, "service saturé");
            case "CLOSED" -> ScoreContribution.excluded("service fermé");
            default -> ScoreContribution.eligible(40.0, "disponibilité non confirmée");
        };
    }
}
