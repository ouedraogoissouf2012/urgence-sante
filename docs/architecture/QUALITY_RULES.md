# Règles de qualité et de maintenabilité

## Taille

- maximum de 300 lignes par fichier écrit manuellement ;
- cible de 30 lignes par méthode ;
- une classe, un widget ou un mapper possède une responsabilité identifiable ;
- un dépassement déclenche un découpage, jamais une désactivation silencieuse.

Exclusions autorisées : code généré, fichiers de verrouillage et migrations SQL
justifiées. Les exclusions sont déclarées explicitement dans le script CI.

## Nommage interdit sans justification

- `Utils`, `Helpers`, `Manager`, `CommonService`, `BaseRepository` ;
- `AppController` ou `GlobalProvider` pilotant plusieurs domaines ;
- fichiers `models` mélangeant DTO, domaine et persistance.

## Back-end

- injection par constructeur uniquement pour les dépendances obligatoires ;
- domaine et application en Java pur ;
- contrôleurs limités à validation, mapping et invocation d’un port entrant ;
- erreurs techniques traduites à la frontière de l’adaptateur ;
- aucun secret ou identifiant d’environnement dans le code ;
- MapStruct réservé aux mappings structurels sans invariant métier.

## Front-end

- flux de données unidirectionnel ;
- état immuable ;
- aucune logique métier dans les widgets ;
- aucune dépendance directe d’une View vers un Service ;
- toutes les couleurs, tailles, espacements et animations viennent du design system ;
- interfaces responsive et accessibles au clavier et aux lecteurs d’écran lorsque applicable.

## Definition of Done

- critères d’acceptation satisfaits ;
- tests automatisés pertinents présents et verts ;
- analyse statique sans erreur ;
- architecture vérifiée ;
- documentation et OpenAPI mises à jour ;
- aucun fichier manuel supérieur à 300 lignes ;
- aucune duplication métier connue introduite.
