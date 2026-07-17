# patient_mobile

Application patient. Réalise le parcours principal : d'un besoin médical vers un
centre recommandé, avec appel des secours ou itinéraire (issue #14).

## Périmètre fonctionnel (Lot 1)

- démarrage, conditions d'utilisation, autorisation de localisation ;
- carte et position de l'utilisateur (OpenStreetMap) ;
- sélection du besoin médical (urgences, maternité, pédiatrie, chirurgie…) ;
- recherche du centre adapté le plus proche et **raison** de la recommandation ;
- itinéraire et temps de trajet ;
- appels d'urgence directs SAMU **185** et Pompiers **180** ;
- états chargement, vide, hors-ligne et erreur soignés.

## Structure d'une fonctionnalité

```text
feature/
├── presentation/   view · view_model · state · widgets
├── domain/         model · repository · use_case
├── data/           service · dto · mapper · repository
└── di/
```

Aucune logique métier dans les widgets. Le contenu Flutter est ajouté aux
issues #5 (squelette) puis #14 (parcours).
