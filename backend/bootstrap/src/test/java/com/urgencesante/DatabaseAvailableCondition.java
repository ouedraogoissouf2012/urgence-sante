package com.urgencesante;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * Condition d'exécution des tests d'intégration base de données, évaluée AVANT
 * le chargement du contexte Spring :
 *
 * <ul>
 *   <li>{@code IT_DB_URL} défini → base externe fournie (validation interne
 *       via Docker Compose) : tests EXÉCUTÉS ;</li>
 *   <li>sinon, Docker joignable par Testcontainers → conteneur éphémère :
 *       tests exécutés ;</li>
 *   <li>sinon → tests ignorés (profil développeur rapide, visibles en
 *       « skipped » ; la garde {@link DockerAvailabilityGuardTest} et le
 *       rapport de {@code scripts/verify-all.sh} empêchent tout skip
 *       silencieux en validation).</li>
 * </ul>
 */
public class DatabaseAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        final String external = System.getenv("IT_DB_URL");
        if (external != null && !external.isBlank()) {
            return ConditionEvaluationResult.enabled("Base externe fournie via IT_DB_URL");
        }
        if (DockerClientFactory.instance().isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker disponible pour Testcontainers");
        }
        return ConditionEvaluationResult.disabled(
                "Profil rapide : ni IT_DB_URL ni Docker joignable — test d'intégration ignoré");
    }
}
