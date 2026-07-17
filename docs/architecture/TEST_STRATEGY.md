# Stratégie de tests d’architecture

## Objectif

Les règles architecturales sont exécutables. Une convention seulement écrite
n’est pas considérée comme protégée.

## Back-end

### Domaine

- tests JUnit sans contexte Spring ;
- invariants des agrégats et value objects ;
- policies et stratégies testées par comportement.

### Application

- cas d’usage testés avec de faux ports ;
- horloge injectable pour tous les comportements temporels ;
- vérification des événements produits.

### Adaptateurs

- tests MVC des DTO, validations, erreurs et statuts HTTP ;
- tests de mapping aux frontières ;
- tests PostgreSQL/PostGIS avec Testcontainers ;
- tests des clients externes avec serveur simulé.

### Modules et architecture

- `ApplicationModules.verify()` bloque cycles et accès internes ;
- règles jMolecules hexagonales en mode strict ;
- règles ArchUnit complémentaires pour DTO, JPA et packages ;
- `@ApplicationModuleTest` vérifie chaque module isolément ;
- Maven Enforcer contrôle les dépendances et versions.

## Flutter

- tests unitaires des services, repositories, use cases et ViewModels ;
- fakes privilégiés pour les contrats stables ;
- widget tests pour les Views et le design system ;
- tests de navigation et d’injection ;
- tests d’intégration pour les parcours critiques ;
- golden tests ciblés sur les composants visuels stables.

## Contrôles de dépôt

- script de limite à 300 lignes avec liste d’exclusions ;
- `mvn verify` pour le back-end ;
- `flutter analyze` et `flutter test` par application/package ;
- validation OpenAPI et génération reproductible du client ;
- détection des secrets et dépendances vulnérables.

## Pyramide de confiance

1. nombreux tests unitaires rapides ;
2. tests d’adaptateurs ciblés ;
3. tests de modules ;
4. peu de tests bout en bout, réservés aux parcours vitaux.
