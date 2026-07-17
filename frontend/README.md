# Front-end — espace de travail Flutter

Deux applications Flutter partagent des fondations visuelles et techniques, mais
gardent des parcours et un métier distincts (voir
[ADR-003](../docs/decisions/ADR-003-flutter-architecture.md)).

## Sous-répertoires

| Répertoire | Responsabilité |
|---|---|
| [`apps/patient_mobile/`](apps/patient_mobile/) | Application patient : parcours d'urgence (carte, recherche, itinéraire, appels). |
| [`apps/hospital_portal/`](apps/hospital_portal/) | Portail agent hospitalier : consultation et mise à jour des disponibilités. |
| [`packages/design_system/`](packages/design_system/) | Tokens, thèmes, composants et layouts — **sans métier**. |
| [`packages/api_client/`](packages/api_client/) | Client REST **généré** depuis OpenAPI. |
| [`packages/app_foundation/`](packages/app_foundation/) | Erreurs, résultat, réseau, observabilité génériques. |
| [`packages/testing_support/`](packages/testing_support/) | Fakes et outils de test réutilisables. |

## Architecture feature-first (MVVM)

```text
View -> ViewModel -> UseCase -> Repository -> Service
```

- flux de données **unidirectionnel**, état immuable ;
- les widgets ne portent **aucune** logique métier et ne contactent jamais Dio,
  le GPS, le cache ou une API directement ;
- une View ne dépend jamais directement d'un Service ;
- Riverpod assemble les dépendances et expose l'état, sans devenir un conteneur
  de règles métier.

## Isolation

Une application ne peut **jamais** importer le code interne de l'autre
application. Le partage passe uniquement par les `packages/` prévus.

Le workspace multi-application (fichiers `pubspec`, environnements
development/staging/production) est initialisé à l'issue #5.
