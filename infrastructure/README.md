# infrastructure

Éléments d'exploitation et d'environnement : base de données géospatiale,
conteneurs et configuration reproductible pour le développement et les tests.

## Contenu

- [`docker-compose.yml`](docker-compose.yml) : **PostgreSQL + PostGIS** local ;
- [`.env.example`](.env.example) : variables attendues (à copier en `.env`) ;
- migrations **Flyway** : [`backend/bootstrap/src/main/resources/db/migration`](../backend/bootstrap/src/main/resources/db/migration).

## Démarrage local

```bash
cp infrastructure/.env.example infrastructure/.env   # puis renseigner le mot de passe
docker compose -f infrastructure/docker-compose.yml --env-file infrastructure/.env up -d
```

L'application lit la base via les variables `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USER`, `DB_PASSWORD` (profil `local`, voir
[`application.yml`](../backend/bootstrap/src/main/resources/application.yml)).
Au démarrage, Flyway applique les migrations et active l'extension PostGIS.

> **Port** : `POSTGRES_PORT` (défaut `5432`) est configurable dans `.env`. Si un
> PostgreSQL tourne déjà sur le port choisi, définissez-en un autre (ex. `55432`)
> et alignez `DB_PORT` côté application.

## Profils

| Profil | Base de données |
|---|---|
| `local` | conteneur `docker-compose` ci-dessus (variables `DB_*`) |
| `test` | conteneur **Testcontainers** éphémère (image `postgis/postgis:16-3.4`) |

Les tests d'intégration nécessitant Docker sont **ignorés automatiquement** si
aucun environnement Docker n'est joignable (`@Testcontainers(disabledWithoutDocker = true)`),
afin de ne pas bloquer un build hors Docker ; ils s'exécutent réellement en CI.

## Règles

- **aucun secret versionné** : les valeurs sensibles passent par des variables
  d'environnement ou des fichiers ignorés (voir [`.gitignore`](../.gitignore)) ;
- les fichiers d'exemple (`*.example`) documentent les variables attendues sans
  valeur réelle ;
- reproductibilité : un poste neuf doit démarrer la base via conteneur sans
  étape manuelle non documentée.
