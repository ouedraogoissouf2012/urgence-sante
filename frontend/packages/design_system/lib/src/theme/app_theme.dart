import 'package:flutter/material.dart';

import '../tokens/app_colors.dart';
import '../tokens/app_radius.dart';
import '../tokens/app_typography.dart';

/// Thèmes des applications, construits à partir des tokens.
abstract final class AppTheme {
  /// Thème de l'application patient (contexte d'urgence).
  static ThemeData patient() => _base(AppColors.patientSeed);

  /// Thème du portail hospitalier (contexte institutionnel).
  static ThemeData hospital() => _base(AppColors.hospitalSeed);

  /// Thème neutre conservé pour compatibilité (équivaut au thème patient).
  static ThemeData light() => patient();

  static ThemeData _base(Color seed) {
    final ColorScheme scheme = ColorScheme.fromSeed(seedColor: seed);
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      // Cibles tactiles confortables (accessibilité, usage sous stress).
      materialTapTargetSize: MaterialTapTargetSize.padded,
      visualDensity: VisualDensity.standard,
      textTheme: const TextTheme(
        headlineSmall: AppTypography.headline,
        titleMedium: AppTypography.title,
        bodyMedium: AppTypography.body,
        bodySmall: AppTypography.caption,
        labelLarge: AppTypography.buttonLabel,
      ),
      cardTheme: const CardThemeData(
        shape: RoundedRectangleBorder(borderRadius: AppRadius.card),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size(64, 48),
          shape: const RoundedRectangleBorder(borderRadius: AppRadius.button),
          textStyle: AppTypography.buttonLabel,
        ),
      ),
    );
  }
}
