# infrastructure

Éléments d'exploitation et d'environnement : base de données géospatiale,
conteneurs et configuration reproductible pour le développement et les tests.

## Contenu prévu

- composition de conteneurs pour **PostgreSQL + PostGIS** (issue #8) ;
- migrations **Flyway** et jeux de données de développement ;
- profils `local` et `test` documentés ;
- configuration d'exploitation (variables d'environnement, hébergement).

## Règles

- **aucun secret versionné** : les valeurs sensibles passent par des variables
  d'environnement ou des fichiers ignorés (voir [`.gitignore`](../.gitignore)) ;
- les fichiers d'exemple (`*.example`) documentent les variables attendues sans
  valeur réelle ;
- reproductibilité : un poste neuf doit démarrer la base via conteneur sans
  étape manuelle non documentée.
