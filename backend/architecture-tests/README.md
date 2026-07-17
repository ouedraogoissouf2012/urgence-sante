# architecture-tests

Rend les règles d'architecture **exécutables** : une convention seulement écrite
n'est pas considérée comme protégée
([TEST_STRATEGY.md](../../docs/architecture/TEST_STRATEGY.md)).

## Gardes prévues

- `ApplicationModules.verify()` (Spring Modulith) : modules fermés,
  `allowedDependencies`, absence de cycles, interdiction d'accès aux packages
  `internal` ;
- règles jMolecules hexagonales en mode strict ;
- règles ArchUnit complémentaires (séparation DTO / Command / domaine / entité
  JPA, contrôleur → pas de repository de persistance) ;
- `@ApplicationModuleTest` par module ;
- contrôle automatique des fichiers manuels de plus de **300 lignes** (voir
  [`scripts/`](../../scripts/)).

## Rôle dans la CI

Ces tests s'exécutent dans `mvn verify` et **bloquent** toute violation
architecturale. Ils sont mis en place à l'issue #4.
