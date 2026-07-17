# scripts

Scripts d'automatisation du dépôt : contrôles de qualité, build et tâches
transverses aux couches back-end et front-end.

## Contenu prévu

- contrôle de la limite des **300 lignes** par fichier manuel, avec liste
  d'exclusions déclarée explicitement (code généré, lock files, migrations) ;
- validation OpenAPI et génération reproductible du client Dart ;
- détection de secrets et de dépendances vulnérables ;
- raccourcis de build/test transverses (`mvn verify`, `flutter analyze`,
  `flutter test`).

## Hook git pre-push

`git-hooks/pre-push` rejoue les gardes localement avant chaque push (équivalent
de la CI). À activer **une fois par clone** :

```bash
git config core.hooksPath scripts/git-hooks
```

Le hook lance `check-file-length.sh` puis `./mvnw verify` (build, tests,
vérification d'architecture). Contournement d'urgence : `git push --no-verify`.

## Règles

- un script échoue de manière visible (code de sortie non nul) en cas de
  violation : il ne masque jamais un problème ;
- toute exclusion d'un contrôle est explicite et justifiée dans le script.
