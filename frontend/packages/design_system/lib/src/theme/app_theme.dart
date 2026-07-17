import 'package:flutter/material.dart';

/// Thème de base partagé par les applications.
///
/// Volontairement minimal à l'issue #5 : les tokens complets (couleurs,
/// typographie, espacements) et les composants arrivent à l'issue #6.
abstract final class AppTheme {
  /// Couleur d'amorce (contexte d'urgence médicale).
  static const Color _seed = Color(0xFFB00020);

  /// Thème clair Material 3.
  static ThemeData light() {
    return ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(seedColor: _seed),
    );
  }
}
