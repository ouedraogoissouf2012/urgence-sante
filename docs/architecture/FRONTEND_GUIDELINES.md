# Lignes directrices front-end (Flutter)

Complète [ADR-003](../decisions/ADR-003-flutter-architecture.md). S'applique aux
applications `patient_mobile` et `hospital_portal`.

## Quand un UseCase (cas d'usage) est requis

Le ViewModel peut appeler directement un repository **uniquement** pour une
lecture/écriture simple sans règle. Un **UseCase dédié** (objet du domaine
testable sans widget) devient obligatoire dès qu'il y a :

- une **règle métier** (choix, seuil, tri, validation au-delà du format) ;
- une **orchestration** de plusieurs sources (repository + service + état) ;
- une **transformation** non triviale entre plusieurs modèles ;
- une logique **réutilisée** par plusieurs ViewModels.

En dessous de ce seuil, un UseCase serait une couche vide : on s'en abstient
(la complexité est une dette, pas une vertu).

## Mappings API → domaine

Les adaptateurs API **ne mappent pas en ligne** : la traduction du client généré
vers les modèles du domaine vit dans un **mapper pur**, sans réseau ni effet de
bord, **testé indépendamment** (ex. `OrientationApiMapper`, testé dans
`orientation_api_mapper_test.dart`).

## Design system

Les widgets n'utilisent **que** les jetons du design system
(`AppColors`, `AppSpacing`, `AppRadius`, `AppTypography`) — jamais de couleur
codée en dur ni de marge numérique. Vérifié par
`test/architecture/design_system_guard_test.dart`.

## Definition of Done — checklist UX/accessibilité

Toute contribution touchant l'interface coche **avant** la revue :

- [ ] **Aucune logique métier ni infrastructure dans les widgets** (View =
      présentation ; règles dans ViewModel/UseCase ; I/O dans repository/service).
- [ ] **Mappings isolés** dans un mapper pur, **testé** séparément.
- [ ] **Semantics** sur les actions critiques : urgence (185/180), appel du
      centre, statut de disponibilité, itinéraire — libellés explicites.
- [ ] **Aucun overflow** : validé en petit écran (≈320 dp), grande et très
      grande police (×2, ×3) et desktop large (test `*_responsive_test.dart`).
- [ ] **Clavier & focus** vérifiés pour les écrans de saisie (portail).
- [ ] **Design system** : aucune valeur de style brute (garde active).
- [ ] **Golden test** ajouté/actualisé pour un rendu critique modifié
      (`flutter test --update-goldens` uniquement sur changement voulu).
- [ ] `flutter analyze` sans avertissement ; tests verts.
