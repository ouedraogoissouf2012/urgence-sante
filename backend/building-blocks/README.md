# building-blocks

Briques transverses **partagées et sans métier**, réutilisables par tous les
modules. Elles ne portent aucune règle de domaine et ne créent aucun couplage
entre modules.

## Contenu prévu

- type `Result` / gestion d'erreurs applicatives génériques ;
- erreurs de base et hiérarchie d'exceptions transverses ;
- identifiants typés et valeurs partagées neutres ;
- abstraction d'horloge (`Clock`) injectable pour les comportements temporels ;
- pagination, tri et primitives de requête réutilisables ;
- contrats d'événements de base pour la publication fiable.

## Interdictions

- aucune dépendance vers un module métier ;
- aucune règle métier spécifique à un domaine ;
- reste minimal : une brique n'est ajoutée que lorsqu'elle est réellement
  partagée par plusieurs modules (éviter l'abstraction spéculative).
