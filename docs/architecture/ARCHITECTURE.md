# Architecture de référence — Urgence Santé

## 1. Statut

- Statut : validée pour le MVP
- Portée : back-end Spring Boot, application patient et portail hospitalier
- Principe directeur : architecture hexagonale, monolithe modulaire et Open/Closed Principle
- Documents sources : cahier des charges technique et Lot 1

Toute dérogation à ce document exige un ADR approuvé.

## 2. Objectifs

L’architecture doit permettre de :

- faire évoluer les règles d’orientation sans réécrire les intégrations ;
- remplacer une technologie externe derrière un port stable ;
- tester le métier sans HTTP, base de données, GPS ou fournisseur cartographique ;
- isoler les domaines fonctionnels et empêcher les dépendances circulaires ;
- partager les fondations visuelles sans partager le métier des applications ;
- maintenir chaque fichier manuel sous 300 lignes.

## 3. Vue du système

```text
Patient Mobile ──┐
                 ├── HTTPS / REST ──> Spring Boot ──> PostgreSQL/PostGIS
Hospital Portal ─┘                         │
                                          ├── OSRM
                                          └── canaux futurs
```

Le back-end est un monolithe modulaire. Chaque domaine est un module fermé avec
une API publique réduite et une implémentation interne hexagonale.

## 4. Organisation du dépôt

```text
urgence-sante/
├── backend/
│   ├── bootstrap/
│   ├── building-blocks/
│   ├── modules/
│   └── architecture-tests/
├── frontend/
│   ├── apps/
│   │   ├── patient_mobile/
│   │   └── hospital_portal/
│   └── packages/
│       ├── design_system/
│       ├── api_client/
│       ├── app_foundation/
│       └── testing_support/
├── infrastructure/
├── docs/
└── scripts/
```

## 5. Modules métier du back-end

| Module | Responsabilité | API publique principale |
|---|---|---|
| `facility` | Établissements, contacts, coordonnées | `FacilityFacade` |
| `medical-service` | Besoins et services médicaux | `MedicalServiceFacade` |
| `availability` | Statuts, fraîcheur, historique | `AvailabilityFacade` |
| `routing` | Itinéraires et temps de trajet | `RoutingFacade` |
| `orientation` | Éligibilité, classement, explication | `OrientationFacade` |
| `identity` | Agents, authentification, autorisations | `IdentityFacade` |
| `audit` | Traçabilité métier | événements publics |
| `notification` | Notifications multicanales | `NotificationFacade` |

Un module ne peut accéder qu’à l’API publique d’un autre module. Tout package
`internal` est privé, même si Java impose une visibilité `public` à un type.

## 6. Anatomie hexagonale d’un module

```text
module/
├── ModuleFacade.java
├── ModuleView.java
├── ModulePublicEvent.java
├── package-info.java
└── internal/
    ├── domain/
    │   ├── model/
    │   ├── policy/
    │   ├── event/
    │   └── exception/
    ├── application/
    │   ├── port/in/
    │   ├── port/out/
    │   ├── command/
    │   ├── query/
    │   ├── result/
    │   └── service/
    ├── adapter/
    │   ├── in/web/
    │   │   ├── dto/request/
    │   │   ├── dto/response/
    │   │   └── mapper/
    │   └── out/persistence/
    │       ├── entity/
    │       ├── repository/
    │       ├── projection/
    │       └── mapper/
    └── configuration/
```

## 7. Rôle des représentations

| Représentation | Couche | Interdiction principale |
|---|---|---|
| Request/Response DTO | Adaptateur REST | ne devient jamais entité métier |
| Command/Query | Application | ne contient pas d’annotation HTTP/JPA |
| Result | Application | ne dépend pas du format JSON |
| Aggregate/Value Object | Domaine | ne dépend d’aucun framework |
| JPA Entity | Adaptateur de persistance | ne sort pas de l’adaptateur |
| Projection | Adaptateur de persistance | ne porte pas de règle métier |
| Public View | API du module | ne révèle pas les classes internes |

## 8. Ports, adaptateurs et repositories

Un port entrant exprime une capacité offerte par l’application :

```text
CreateFacilityUseCase
FindSuitableFacilitiesUseCase
UpdateAvailabilityUseCase
```

Un port sortant exprime un besoin du métier :

```text
LoadFacilityPort
SaveAvailabilityPort
EstimateTravelTimePort
PublishDomainEventPort
```

Le repository métier est un port et manipule le domaine. Un repository Spring
Data est un détail de persistance et manipule uniquement les entités JPA. Un
adaptateur de persistance relie les deux à l’aide d’un mapper dédié.

## 9. Règle de dépendance

```text
adapter/in ──> application ──> domain
adapter/out ─> application ──> domain
configuration ─> toutes les couches du même module
```

Interdictions :

- domaine vers Spring, JPA, Jackson ou MapStruct ;
- application vers contrôleur, JPA ou fournisseur externe ;
- contrôleur vers repository de persistance ;
- module vers le package `internal` d’un autre module ;
- cycle entre modules.

## 10. Transactions et événements

- la frontière transactionnelle appartient au cas d’usage applicatif ;
- aucun appel réseau externe ne reste dans une transaction de base ;
- les réactions secondaires intermodules utilisent des événements ;
- un appel synchrone intermodule est réservé à une réponse immédiate nécessaire ;
- les événements critiques utilisent un mécanisme de publication fiable.

## 11. Architecture Flutter

Les deux applications suivent une structure feature-first et un flux
unidirectionnel :

```text
View -> ViewModel -> UseCase -> Repository -> Service
```

Une fonctionnalité complexe contient :

```text
feature/
├── presentation/
│   ├── view/
│   ├── view_model/
│   ├── state/
│   └── widgets/
├── domain/
│   ├── model/
│   ├── repository/
│   └── use_case/
├── data/
│   ├── service/
│   ├── dto/
│   ├── mapper/
│   └── repository/
└── di/
```

Les widgets ne portent aucune logique métier et ne contactent jamais Dio, le
GPS, le cache ou une API directement. Riverpod assemble les dépendances et
expose les états, sans devenir un conteneur de règles métier.

## 12. Packages front-end partagés

- `design_system` : tokens, thèmes, composants et layouts sans métier ;
- `api_client` : client généré depuis OpenAPI ;
- `app_foundation` : erreurs, résultat, réseau et observabilité génériques ;
- `testing_support` : fakes et outils de test réutilisables.

Une application ne peut jamais importer le code interne de l’autre application.

## 13. Points d’extension

Les extensions prévues sont documentées dans `docs/extension-points/` :

- stratégie d’orientation ;
- fournisseur d’itinéraire ;
- source d’établissements ;
- canal de notification ;
- fournisseur d’identité.

## 14. Gouvernance

- toute nouvelle dépendance intermodule est revue explicitement ;
- toute modification d’une décision structurante exige un ADR ;
- la CI bloque une violation architecturale ou un fichier manuel supérieur à
  300 lignes ;
- les choix spécifiques à une fonctionnalité restent documentés dans son module.
