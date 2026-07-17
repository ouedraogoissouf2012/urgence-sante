# app_foundation

Fondations techniques **génériques** partagées par les applications, sans métier
ni dépendance à un domaine particulier.

## Contenu prévu

- type résultat et gestion d'erreurs applicatives ;
- primitives réseau (intercepteurs, politique de délai/retry) neutres ;
- observabilité générique (journalisation, traces) ;
- utilitaires transverses réutilisables.

## Interdictions

- aucune règle métier ;
- aucune dépendance vers une application ou vers `design_system`.
