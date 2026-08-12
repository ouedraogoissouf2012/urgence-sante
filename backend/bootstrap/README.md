# bootstrap

Module d'**assemblage** de l'application Spring Boot. C'est le seul point où les
modules métier sont réunis et où l'application est démarrée.

## Responsabilités

- classe de démarrage `main` et contexte Spring Boot ;
- configuration globale et profils (`local`, `test`, `staging`, `production`) ;
- composition des modules métier et de leurs configurations ;
- exposition des préoccupations transverses (sécurité HTTP, gestion centralisée
  des erreurs, observabilité) branchées sur les modules ;
- **seul propriétaire des migrations Flyway** (voir ci-dessous).

## Interdictions

- aucune règle métier ;
- aucun accès au package `internal` d'un module ;
- aucun secret ou identifiant d'environnement versionné.

## Migrations Flyway (issue #153)

Toutes les migrations de schéma vivent exclusivement dans
[`src/main/resources/db/migration`](src/main/resources/db/migration), quel que
soit le module métier concerné. Un module ne doit **plus jamais** définir son
propre dossier `db/migration` : `flyway.locations` ne scanne que
`classpath:db/migration`, et un second dossier local dans un module créerait
de nouveau la possibilité d'une collision de numéro de version silencieuse
entre deux travaux menés en parallèle — exactement l'incident du 10 août
(panne prod ~2h, commit `5a894185`) qu'on cherche à rendre impossible
structurellement plutôt que de recorriger au cas par cas.

Règles pour toute nouvelle migration :

1. **Un seul dossier, une seule séquence globale.** Avant d'ajouter un
   fichier, regarder le numéro `V` le plus élevé déjà présent dans ce dossier
   et prendre le suivant — jamais une numérotation relative à un module.
2. **Préfixe de module obligatoire dans le nom du fichier**, pour que
   l'origine reste lisible même une fois tous les fichiers réunis :
   `V<n>__<module>__<description>.sql` (ex. `V13__identity__add_mfa_secret.sql`).
3. **Ne jamais renommer une migration déjà appliquée.** Flyway calcule la
   description d'une migration à partir de son nom de fichier et la compare,
   au démarrage, à celle enregistrée dans `flyway_schema_history` pour cette
   version ; un simple renommage d'un fichier déjà migré (donc tout fichier
   de `V1` à `V12`, déjà déployés en production) produit une erreur de
   validation `DESCRIPTION_MISMATCH` et empêche l'application de démarrer.
   C'est pour cette raison que les migrations `V2`–`V4` et `V6`–`V12`,
   régularisées ici depuis les anciens dossiers par module (`V1` et `V5`
   vivaient déjà dans `bootstrap`), ont été **déplacées telles quelles**
   (nom et contenu inchangés) plutôt que renommées avec le préfixe
   de module : seules les migrations créées à partir de `V13` suivent la
   convention `__<module>__` ci-dessus.
