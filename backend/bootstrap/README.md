# bootstrap

Module d'**assemblage** de l'application Spring Boot. C'est le seul point où les
modules métier sont réunis et où l'application est démarrée.

## Responsabilités

- classe de démarrage `main` et contexte Spring Boot ;
- configuration globale et profils (`local`, `test`, `staging`, `production`) ;
- composition des modules métier et de leurs configurations ;
- exposition des préoccupations transverses (sécurité HTTP, gestion centralisée
  des erreurs, observabilité) branchées sur les modules.

## Interdictions

- aucune règle métier ;
- aucun accès au package `internal` d'un module ;
- aucun secret ou identifiant d'environnement versionné.

Le contenu Maven et la classe de démarrage sont ajoutés à l'issue #3.
