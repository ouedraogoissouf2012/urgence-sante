package com.urgencesante.orientation.internal.domain.strategy;

import com.urgencesante.orientation.internal.domain.model.AvailabilityRating;
import com.urgencesante.orientation.internal.domain.model.CandidateEvaluation;
import com.urgencesante.orientation.internal.domain.model.ScoreContribution;

/**
 * Classe selon le statut de disponibilité. Un établissement fermé est exclu ;
 * un statut non confirmé (dont un statut périmé, déjà ramené à « UNKNOWN » par
 * le moteur) reste éligible mais moins favorisé. Score, éligibilité et raison
 * proviennent d'{@link AvailabilityRating} (source unique de vérité).
 */
public class AvailabilityStrategy implements OrientationStrategy {

    @Override
    public ScoreContribution evaluate(CandidateEvaluation candidate) {
        final AvailabilityRating rating = AvailabilityRating.of(candidate.status());
        return rating.eligible()
                ? ScoreContribution.eligible(rating.score(), rating.reason())
                : ScoreContribution.excluded(rating.reason());
    }
}
