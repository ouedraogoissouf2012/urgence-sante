import 'package:flutter/widgets.dart';

/// Tokens de rayons d'arrondi.
abstract final class AppRadius {
  static const Radius sm = Radius.circular(4);
  static const Radius md = Radius.circular(8);

  /// Rayon intermédiaire des cartes standard : adoucit la transition entre les
  /// petits éléments (badge, 8) et les grandes cartes (20), pour un langage
  /// d'arrondis cohérent avec la feuille glissante (24).
  static const Radius mdLarge = Radius.circular(12);

  static const Radius lg = Radius.circular(16);

  /// Rayon des cartes de contenu mises en avant (proche de la feuille : 24).
  static const Radius xl = Radius.circular(20);

  /// Rayon « pilule » : coins totalement arrondis (boutons d'action).
  static const Radius pill = Radius.circular(999);

  /// Carte de contenu standard (bannières, cartes compactes, conteneurs).
  static const BorderRadius card = BorderRadius.all(mdLarge);

  /// Carte de contenu proéminente (ex. centre recommandé mis en avant).
  static const BorderRadius cardLarge = BorderRadius.all(xl);

  /// Petit conteneur étiquette (ex. badge de statut) : aligné sur `md` pour
  /// s'accorder aux cartes plutôt que de rester anguleux (ex-`sm`).
  static const BorderRadius badge = BorderRadius.all(md);

  /// Forme des boutons : pilule. Changer ici modifie tous les boutons de
  /// l'application (le thème dérive sa forme de ce token).
  static const BorderRadius button = BorderRadius.all(pill);

  /// Rayon du haut d'une feuille glissante (bottom sheet).
  static const BorderRadius sheetTop =
      BorderRadius.vertical(top: Radius.circular(24));
}
