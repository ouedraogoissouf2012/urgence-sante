package com.urgencesante.orientation.internal.domain.strategy;

import com.urgencesante.orientation.internal.domain.model.CandidateEvaluation;
import com.urgencesante.orientation.internal.domain.model.ScoreContribution;

/**
 * Stratégie de classement d'un candidat. Point d'extension : de nouvelles
 * stratégies s'ajoutent (enregistrées dans la configuration) sans modifier le
 * moteur d'orientation (principe ouvert/fermé).
 */
public interface OrientationStrategy {

    ScoreContribution evaluate(CandidateEvaluation candidate);
}
