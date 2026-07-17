# modules

Modules métier **fermés**. Chaque module expose une API publique réduite
(`ModuleFacade`, `ModuleView`, événements publics) et garde son implémentation
dans un package `internal` privé, même si Java impose une visibilité `public`.

## Modules prévus

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

## Anatomie hexagonale d'un module

```text
module/
├── ModuleFacade.java        API publique (capacités offertes)
├── ModuleView.java          Vue publique (ne révèle pas l'interne)
├── ModulePublicEvent.java   Événements publics
├── package-info.java        Déclaration du module et dépendances autorisées
└── internal/
    ├── domain/              model · policy · event · exception (Java pur)
    ├── application/         port/in · port/out · command · query · result · service
    ├── adapter/
    │   ├── in/web/          contrôleurs · dto/request · dto/response · mapper
    │   └── out/persistence/ entity · repository · projection · mapper
    └── configuration/
```

## Règles

- un module n'accède qu'à l'**API publique** d'un autre module ;
- aucun cycle entre modules (voir
  [`MODULE_DEPENDENCIES.md`](../../docs/architecture/MODULE_DEPENDENCIES.md)) ;
- réactions secondaires intermodules via **événements**, appel synchrone réservé
  à une réponse immédiate nécessaire ;
- les modules sont implémentés à partir de l'issue #9, chacun dans sa propre
  issue et sa propre PR.
