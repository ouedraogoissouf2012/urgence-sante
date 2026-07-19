# ADR-005 — Périmètre de l'annuaire d'Abidjan et traçabilité

- Statut : accepté
- Date : 2026-07-19

## Contexte

Le Lot 1 annonçait « environ 150 à 300 établissements d'Abidjan fiabilisés ».
Le dépôt ne contenait que 15 centres explicitement simulés. Fabriquer 150-300
établissements sans source vérifiable produirait de **fausses données** — inacceptable
pour une application d'orientation médicale.

## Décision

Le **périmètre de volume est officiellement révisé** : le MVP ne prétend pas
livrer 150-300 établissements vérifiés. Il livre à la place :

1. un **schéma traçable** — chaque établissement porte `source`, `external_ref`,
   `verified_at`, `steward` et `data_status` (VERIFIED / PROVISIONAL / DEMO) ;
2. un **pipeline d'import idempotent et validé** (zone d'Abidjan, téléphone
   ivoirien, services connus, doublons), produisant un **rapport de revue** ;
3. un **jeu de démarrage réel mais PROVISOIRE** (`infrastructure/directory/
   abidjan-starter.json`) : établissements publics connus, à confirmer par une
   revue manuelle (aucun n'est marqué VERIFIED tant qu'il n'est pas vérifié) ;
4. une **isolation stricte des données fictives** : le statut DEMO ne peut pas
   être chargé en production (refus à l'import) et le démarrage en production
   échoue si des lignes DEMO existent.

Le volume cible (150-300 VERIFIED) devient un **objectif de collecte de données**,
atteint en important des lots vérifiés au fil de l'eau via le pipeline — sans
changement de code.

## Conséquences

- Aucune donnée inventée n'est présentée comme réelle (dette explicite : le
  volume cible reste à alimenter par des sources vérifiées).
- La démonstration conserve ses 15 centres, désormais marqués `DEMO` et
  cantonnés au profil de démonstration.
- La montée en volume est opérationnelle (import de fichiers), pas un travail
  de développement.
