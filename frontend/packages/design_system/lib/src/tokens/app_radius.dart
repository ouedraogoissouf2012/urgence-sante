import 'package:flutter/widgets.dart';

/// Tokens de rayons d'arrondi.
abstract final class AppRadius {
  static const Radius sm = Radius.circular(4);
  static const Radius md = Radius.circular(8);
  static const Radius lg = Radius.circular(16);

  /// Rayon des cartes de contenu mises en avant (plus douces).
  static const Radius xl = Radius.circular(22);

  /// Rayon « pilule » : coins totalement arrondis (boutons d'action).
  static const Radius pill = Radius.circular(999);

  static const BorderRadius card = BorderRadius.all(md);

  /// Carte de contenu proéminente (ex. centre recommandé mis en avant).
  static const BorderRadius cardLarge = BorderRadius.all(xl);

  /// Forme des boutons : pilule. Changer ici modifie tous les boutons de
  /// l'application (le thème dérive sa forme de ce token).
  static const BorderRadius button = BorderRadius.all(pill);

  /// Rayon du haut d'une feuille glissante (bottom sheet).
  static const BorderRadius sheetTop =
      BorderRadius.vertical(top: Radius.circular(24));
}
