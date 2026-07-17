# hospital_portal

Portail des agents hospitaliers. Permet à un agent de consulter et de mettre à
jour les statuts de disponibilité de son établissement (issue #15).

## Périmètre fonctionnel

- connexion de l'agent ;
- affichage des services de l'établissement ;
- mise à jour de statut **horodatée** ;
- historique des changements visible ;
- interface responsive et accessible.

## Structure d'une fonctionnalité

```text
feature/
├── presentation/   view · view_model · state · widgets
├── domain/         model · repository · use_case
├── data/           service · dto · mapper · repository
└── di/
```

Aucune logique métier dans les widgets ; aucune dépendance directe d'une View
vers un Service. Le contenu Flutter est ajouté aux issues #5 (squelette) puis
#15 (portail).
