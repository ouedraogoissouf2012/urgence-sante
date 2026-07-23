import 'dart:math' as math;
import 'dart:ui';

/// Calcul du contraste WCAG 2.x, partagé par les gardes d'accessibilité.
///
/// Sépare le calcul (ici, testable et unique) des gardes qui l'utilisent :
/// aucune garde ne recopie la formule, une correction profite à toutes.
abstract final class WcagContrast {
  /// Seuil AA pour le texte normal (le plus courant dans l'interface).
  static const double aaNormalText = 4.5;

  /// Seuil AA pour le gros texte et les éléments d'interface non textuels.
  static const double aaLargeText = 3.0;

  /// Linéarise une composante sRGB (0..1) selon WCAG 2.x.
  static double _linear(double c) => c <= 0.03928
      ? c / 12.92
      : math.pow((c + 0.055) / 1.055, 2.4).toDouble();

  /// Luminance relative d'une couleur **opaque** (le canal alpha est ignoré :
  /// composer une couleur translucide d'abord avec [composite]).
  static double luminance(Color color) =>
      0.2126 * _linear(color.r) +
      0.7152 * _linear(color.g) +
      0.0722 * _linear(color.b);

  /// Ratio de contraste entre deux couleurs opaques (1:1 à 21:1).
  static double ratio(Color a, Color b) {
    final double la = luminance(a);
    final double lb = luminance(b);
    return (math.max(la, lb) + 0.05) / (math.min(la, lb) + 0.05);
  }

  /// Compose un premier plan translucide [fg] (à l'opacité [alpha]) sur un fond
  /// **opaque** [background], et renvoie la couleur opaque résultante — ce que
  /// l'œil perçoit réellement. Indispensable pour les fonds translucides
  /// (badges teintés à 12 %, surfaces à alpha).
  static Color composite(Color fg, double alpha, Color background) => Color.from(
        alpha: 1,
        red: fg.r * alpha + background.r * (1 - alpha),
        green: fg.g * alpha + background.g * (1 - alpha),
        blue: fg.b * alpha + background.b * (1 - alpha),
      );
}
