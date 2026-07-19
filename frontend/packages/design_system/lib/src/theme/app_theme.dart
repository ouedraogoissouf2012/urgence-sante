import 'package:flutter/material.dart';

import '../tokens/app_colors.dart';
import '../tokens/app_radius.dart';
import '../tokens/app_sizing.dart';
import '../tokens/app_typography.dart';

/// Thèmes des applications, construits à partir des tokens.
///
/// Source unique de vérité : la forme des boutons, l'arrondi des cartes, la
/// typographie et les tailles tactiles dérivent tous des tokens du design
/// system. Modifier un token (couleur d'amorce, rayon, taille) repeint
/// l'ensemble des écrans sans toucher au code applicatif.
abstract final class AppTheme {
  /// Thème de l'application patient (contexte d'urgence).
  static ThemeData patient() => _base(AppColors.patientSeed);

  /// Thème du portail hospitalier (contexte institutionnel).
  static ThemeData hospital() => _base(AppColors.hospitalSeed);

  /// Thème neutre conservé pour compatibilité (équivaut au thème patient).
  static ThemeData light() => patient();

  static ThemeData _base(Color seed) {
    final ColorScheme scheme = ColorScheme.fromSeed(seedColor: seed);
    const RoundedRectangleBorder pillShape =
        RoundedRectangleBorder(borderRadius: AppRadius.button);
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
      // Boutons remplis : forme pilule dérivée du token.
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: AppSizing.buttonMin,
          shape: pillShape,
          textStyle: AppTypography.buttonLabel,
        ),
      ),
      // Boutons contour : même forme pilule (cohérence Itinéraire/Appeler).
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          minimumSize: AppSizing.buttonMin,
          shape: pillShape,
          textStyle: AppTypography.buttonLabel,
          side: BorderSide(color: scheme.primary),
        ),
      ),
      // Feuille glissante : haut arrondi dérivé du token.
      bottomSheetTheme: const BottomSheetThemeData(
        shape: RoundedRectangleBorder(borderRadius: AppRadius.sheetTop),
      ),
    );
  }
}
