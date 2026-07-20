import 'package:flutter/widgets.dart';

/// Tokens de tailles (cibles tactiles et icônes).
///
/// Centralise les dimensions autrefois codées en dur dans les composants, afin
/// que le design system reste la source unique de vérité (aucune valeur brute
/// dans les widgets applicatifs).
abstract final class AppSizing {
  /// Cible tactile minimale d'un bouton standard.
  static const Size buttonMin = Size(64, 48);

  /// Cible tactile minimale d'un bouton d'urgence (plus généreuse : usage
  /// sous stress).
  static const Size emergencyButtonMin = Size(120, 56);

  /// Taille d'une icône illustrative (états vides, erreurs).
  static const double iconLarge = 48;

  /// Taille de l'icône d'en-tête d'un écran (accueil).
  static const double iconHero = 64;

  /// Taille d'une icône de marqueur cartographique.
  static const double iconMarker = 32;

  /// Diamètre du marqueur d'un centre sur la carte.
  static const double markerTap = 44;

  /// Diamètre d'un marqueur de centre non sélectionné.
  static const double markerCenter = 34;
}
