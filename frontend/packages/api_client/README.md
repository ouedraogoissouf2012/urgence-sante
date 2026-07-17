# api_client

Client REST **généré** depuis la spécification OpenAPI v1 (voir
[ADR-004](../../../docs/decisions/ADR-004-contract-first-api.md)).

## Principes

- le code généré est isolé dans un sous-dossier `generated/` dédié ;
- la génération est **reproductible** depuis la spécification validée ;
- les fichiers générés sont exclus de la limite des 300 lignes et de l'analyse
  manuelle ;
- toute divergence front/back est détectée à la génération, pas à l'exécution.

## Interdictions

- aucune retouche manuelle du code généré ;
- aucune règle métier ajoutée dans ce package.

La spécification OpenAPI et la chaîne de génération sont définies à l'issue #7.
