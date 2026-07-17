# ADR-003 — Front-end feature-first MVVM

- Statut : accepté
- Date : 2026-07-16

## Contexte

Deux applications Flutter partagent des fondations visuelles et techniques,
mais possèdent des parcours et responsabilités métier différents.

## Décision

Les applications utilisent une organisation feature-first : View, ViewModel,
domain facultatif selon la complexité, repository et service. Riverpod fournit
l’injection et l’observation d’état. Les données suivent un flux unidirectionnel.

## Conséquences

- logique d’interface testable sans widget ;
- aucune dépendance directe des Views vers les services ;
- partage limité aux packages explicitement prévus ;
- discipline requise pour ne pas déplacer le métier dans les providers.
