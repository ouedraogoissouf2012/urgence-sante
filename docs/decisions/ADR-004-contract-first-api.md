# ADR-004 — API contract-first

- Statut : accepté
- Date : 2026-07-16

## Contexte

Le mobile, le portail et le back-end doivent évoluer sans dupliquer manuellement
les contrats et modèles réseau.

## Décision

L’API publique est versionnée sous `/api/v1` et décrite par OpenAPI. Le client
Dart est généré de manière reproductible depuis la spécification validée.

## Conséquences

- divergence front/back détectée plus tôt ;
- code généré isolé dans `api_client/generated` ;
- modifications incompatibles soumises à revue ;
- fichiers générés exclus de la limite de 300 lignes.
