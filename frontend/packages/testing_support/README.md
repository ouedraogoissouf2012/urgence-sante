# testing_support

Outils de test **réutilisables** : fakes, doubles et helpers partagés par les
applications et les autres packages.

## Contenu prévu

- fakes des contrats stables (repositories, services, ports) privilégiés aux
  mocks générés ;
- fabriques de données de test ;
- utilitaires de configuration de tests de widgets et d'injection.

## Interdictions

- aucune dépendance en production (package réservé aux tests) ;
- aucune règle métier réelle : uniquement des doubles contrôlés.
