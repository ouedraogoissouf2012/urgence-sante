# Matrice des dépendances

## Principe

Une case autorisée signifie qu’un module peut dépendre uniquement de l’API
publique du module cible. Elle n’autorise jamais l’accès à son package
`internal`, ses entités JPA ou ses repositories techniques.

| Source | facility | medical-service | availability | routing | identity | audit | notification |
|---|---:|---:|---:|---:|---:|---:|---:|
| facility | — | non | non | non | non | événements | non |
| medical-service | API | — | non | non | non | événements | non |
| availability | API | API | — | non | API limitée | événements | événements |
| routing | non | non | non | — | non | événements | non |
| orientation | API | API | API | API | non | événements | non |
| identity | non | non | non | non | — | événements | événements |
| audit | non | non | non | non | non | — | non |
| notification | non | non | non | non | non | événements | — |

## Règles

1. `orientation` orchestre des vues publiques et ne possède pas les données des
   autres modules.
2. `audit` consomme des événements ; les modules métier ne l’appellent pas
   directement.
3. `notification` réagit aux événements ou à un port applicatif explicite.
4. `availability` peut vérifier l’identité et les droits d’un agent par une API
   étroite, sans lire les tables d’identité.
5. `routing` reste indépendant des établissements et reçoit des coordonnées.
6. Toute nouvelle flèche exige la mise à jour de cette matrice et un ADR.

## Contrôles attendus

- Spring Modulith : modules fermés et `allowedDependencies` ;
- ArchUnit/jMolecules : dépendances hexagonales et absence de cycles ;
- Maven : dépendances physiques explicites entre modules ;
- revue : aucune dépendance vers un package `internal`.
