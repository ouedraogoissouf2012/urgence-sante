# Points d’extension

## Règle générale

Une extension implémente un port stable et est enregistrée dans la configuration.
Elle ne modifie pas le domaine existant sauf si une règle métier elle-même change.

## OrientationStrategy

Permet d’ajouter une méthode de classement des candidats.

Invariants :

- ne crée pas de disponibilité inexistante ;
- traite une donnée périmée comme non confirmée ;
- retourne une explication compréhensible ;
- classement déterministe pour des entrées identiques.

## RouteProviderPort

Permet d’ajouter OSRM, GraphHopper ou un fournisseur futur.

Invariants : timeout borné, erreurs normalisées, aucun appel dans une transaction
de base et métriques par fournisseur.

## FacilityDataSourcePort

Permet l’import CSV, open data, OpenStreetMap ou API partenaire.

Invariants : provenance, date de collecte, validation, déduplication et rapport
d’erreurs conservés.

## NotificationChannelPort

Permet push, SMS ou e-mail.

Invariants : idempotence, consentement applicable, erreurs observables et aucune
donnée sensible inutile dans le message.

## IdentityProviderPort

Permet mot de passe, OTP ou fournisseur d’identité futur.

Invariants : identité normalisée, autorisations décidées côté serveur, secrets
hors du code et audit des opérations sensibles.
