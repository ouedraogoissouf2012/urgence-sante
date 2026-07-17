# ADR-001 — Monolithe modulaire

- Statut : accepté
- Date : 2026-07-16

## Contexte

Le MVP doit être livré rapidement tout en préservant des frontières métier
capables d’évoluer. Des microservices ajouteraient réseau, déploiements,
observabilité distribuée et cohérence de données avant que ces coûts soient
justifiés.

## Décision

Le back-end est un monolithe modulaire Maven/Spring Boot. Chaque domaine est un
module fermé vérifié par Spring Modulith et des tests d’architecture.

## Conséquences

- déploiement et transactions simples pour le MVP ;
- frontières fonctionnelles explicites ;
- extraction future possible après preuve d’un besoin opérationnel ;
- discipline obligatoire sur les API publiques et dépendances intermodules.
