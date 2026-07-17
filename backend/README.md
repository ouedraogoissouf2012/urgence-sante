# Back-end — monolithe modulaire

Back-end Spring Boot organisé en **monolithe modulaire** (voir
[ADR-001](../docs/decisions/ADR-001-modular-monolith.md)). Chaque domaine
fonctionnel est un module fermé exposant une API publique réduite et gardant une
implémentation interne hexagonale (voir
[ADR-002](../docs/decisions/ADR-002-hexagonal-architecture.md)).

## Sous-répertoires

| Répertoire | Responsabilité |
|---|---|
| [`bootstrap/`](bootstrap/) | Assemblage et démarrage de l'application, configuration globale, profils. Ne contient aucune règle métier. |
| [`building-blocks/`](building-blocks/) | Briques transverses partagées **sans métier** : type résultat, erreurs de base, identifiants, horloge, pagination. |
| [`modules/`](modules/) | Modules métier fermés (`facility`, `medical-service`, `availability`, `routing`, `orientation`, `identity`, `audit`, `notification`). |
| [`architecture-tests/`](architecture-tests/) | Règles d'architecture **exécutables** : Spring Modulith, ArchUnit/jMolecules, contrôle des 300 lignes. |

## Règle de dépendance

```text
adapter/in  ──> application ──> domain
adapter/out ──> application ──> domain
configuration ──> toutes les couches du même module
```

- le domaine et l'application restent en **Java pur** (aucune dépendance Spring,
  JPA, Jackson ou MapStruct) ;
- un module ne dépend que de l'**API publique** d'un autre module, jamais de son
  package `internal` ;
- aucun cycle entre modules ; la matrice autorisée fait foi
  ([`MODULE_DEPENDENCIES.md`](../docs/architecture/MODULE_DEPENDENCIES.md)).

## Build

Le socle Maven multi-module (parent + modules) est mis en place à l'issue #3. À
ce stade, ce répertoire ne contient que la structure d'accueil, sans code.
