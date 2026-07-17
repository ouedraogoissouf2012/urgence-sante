# Urgence Santé

Application d'orientation vers les urgences médicales du Grand Abidjan. En
situation d'urgence, l'application oriente une personne vers l'établissement de
santé adapté le plus proche, avec itinéraire, temps de trajet et appel direct
des secours (SAMU 185, Pompiers 180).

> Périmètre du premier lot (Lot 1) : voir [`Lot1_Livraison_1_Mois.md`](Lot1_Livraison_1_Mois.md).
> La disponibilité « temps réel » des services est hors périmètre du Lot 1.

## Stack

| Couche | Technologie |
|---|---|
| Mobile / portail | Flutter (Dart), Riverpod, feature-first MVVM |
| Back-end | Spring Boot (Java), monolithe modulaire hexagonal |
| Base de données | PostgreSQL + PostGIS |
| Itinéraires | OSRM (derrière un port `RouteProviderPort`) |
| Contrat d'API | OpenAPI v1, client Dart généré |

## Arborescence du dépôt

```text
urgence-sante/
├── backend/                  Monolithe modulaire Spring Boot (hexagonal)
│   ├── bootstrap/            Assemblage de l'application, démarrage, config globale
│   ├── building-blocks/      Briques partagées sans métier (résultat, erreurs, ids)
│   ├── modules/              Modules métier fermés (facility, routing, orientation…)
│   └── architecture-tests/   Gardes exécutables (Spring Modulith, ArchUnit, 300 lignes)
├── frontend/                 Espace de travail Flutter multi-applications
│   ├── apps/
│   │   ├── patient_mobile/   Application patient (parcours d'urgence)
│   │   └── hospital_portal/  Portail de saisie de disponibilité (agents hospitaliers)
│   └── packages/
│       ├── design_system/    Tokens, thèmes, composants et layouts (sans métier)
│       ├── api_client/        Client REST généré depuis OpenAPI
│       ├── app_foundation/    Erreurs, résultat, réseau, observabilité génériques
│       └── testing_support/   Fakes et outils de test réutilisables
├── infrastructure/           Conteneurs, base géospatiale, exploitation locale
├── docs/                     Architecture de référence, ADR, points d'extension
└── scripts/                  Scripts de qualité, build et automatisation du dépôt
```

Chaque répertoire porte un `README.md` décrivant sa responsabilité et ses
contraintes. L'architecture de référence fait foi :
[`docs/architecture/ARCHITECTURE.md`](docs/architecture/ARCHITECTURE.md).

## Règles structurantes

- Architecture hexagonale et principe ouvert/fermé (voir les ADR).
- Un module n'accède qu'à l'**API publique** d'un autre module, jamais à son
  package `internal`.
- Aucun fichier écrit manuellement ne dépasse **300 lignes**
  ([`QUALITY_RULES.md`](docs/architecture/QUALITY_RULES.md)).
- Toute dérogation à une décision structurante exige un **ADR** approuvé.

## Documentation

| Document | Objet |
|---|---|
| [`docs/architecture/ARCHITECTURE.md`](docs/architecture/ARCHITECTURE.md) | Architecture de référence |
| [`docs/architecture/MODULE_DEPENDENCIES.md`](docs/architecture/MODULE_DEPENDENCIES.md) | Matrice des dépendances intermodules |
| [`docs/architecture/QUALITY_RULES.md`](docs/architecture/QUALITY_RULES.md) | Règles de qualité et Definition of Done |
| [`docs/architecture/TEST_STRATEGY.md`](docs/architecture/TEST_STRATEGY.md) | Stratégie de tests d'architecture |
| [`docs/decisions/`](docs/decisions/) | Décisions d'architecture (ADR) |
| [`docs/extension-points/`](docs/extension-points/README.md) | Points d'extension stables |

## État d'avancement

Le suivi se fait par issues GitHub (une par lot de travail, avec dépendances).
Le squelette du dépôt (cette étape) ne contient **aucun code métier** : il
prépare l'accueil des modules back-end et des applications Flutter.
