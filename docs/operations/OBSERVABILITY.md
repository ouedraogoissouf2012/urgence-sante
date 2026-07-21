# Observabilité

## Santé (états distincts)

| Endpoint | Signification |
|---|---|
| `GET /actuator/health/liveness` | **Démarré** : le processus vit (redémarrer si DOWN). |
| `GET /actuator/health/readiness` | **Prêt** : peut servir — inclut la base de données (`db`). |
| `GET /actuator/health` (composant `osrm`) | **Dégradé** : `DEGRADED` quand le disjoncteur OSRM est ouvert — l'application reste PRÊTE (temps estimés). N'appartient volontairement pas au groupe readiness. |

`scripts/local-up.sh` attend la readiness (pas un endpoint métier).

## Corrélation

Chaque requête porte un identifiant : en-tête **`X-Correlation-Id`** propagé
s'il est fourni (format `[A-Za-z0-9._-]{8,64}`), généré sinon ; renvoyé dans la
réponse et présent dans **chaque ligne de log** (`%X{correlationId}`) ainsi que
dans l'événement `AvailabilityUpdated.correlationId`. Aucune donnée sensible
(jamais de position) n'est journalisée.

## Logs par environnement

- défaut/local : texte lisible avec corrélation ;
- **production** : JSON structuré **ECS** (`logging.structured.format.console`),
  détails de santé masqués.

## Métriques (`GET /actuator/metrics/{nom}`)

| Métrique | Usage |
|---|---|
| `http.server.requests{uri=/api/v1/orientation}` | Latence (max/percentiles) et volume des recommandations. |
| `http.server.requests{uri=/api/v1/facilities/{facilityId}/availability/{serviceCode}, method=PUT}` | Mises à jour de disponibilité. |
| `osrm.errors` | Échecs d'appels OSRM (avant ouverture du disjoncteur). |
| composant santé `osrm.details.circuit` | État du disjoncteur (CLOSED/HALF_OPEN/OPEN). |
