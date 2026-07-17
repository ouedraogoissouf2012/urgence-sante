# ADR-002 — Architecture hexagonale par module

- Statut : accepté
- Date : 2026-07-16

## Contexte

La solution dépend de technologies susceptibles d’évoluer : PostGIS, OSRM,
authentification, notifications, cartographie et stockage local.

## Décision

Chaque module métier applique Ports & Adapters : domaine, application, ports,
adaptateurs entrants, adaptateurs sortants et configuration d’assemblage.

Le domaine et les services applicatifs restent indépendants de Spring et JPA.

## Conséquences

- métier testable sans infrastructure ;
- fournisseurs externes remplaçables ;
- davantage de types aux frontières ;
- séparation obligatoire entre DTO, commandes, domaine et entités JPA.
