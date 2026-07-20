import 'package:flutter/material.dart';

import '../tokens/app_colors.dart';
import '../tokens/app_radius.dart';
import '../tokens/app_sizing.dart';
import '../tokens/app_typography.dart';

/// Thèmes des applications, construits à partir des tokens.
///
/// Source unique de vérité : forme des boutons, arrondi des cartes, typographie
/// et tailles tactiles dérivent des tokens du design system. Modifier un token
/// repeint l'ensemble des écrans sans toucher au code applicatif.
abstract final class AppTheme {
  /// Thème patient clair (contexte d'urgence).
  static ThemeData patient() =>
      _base(AppColors.patientSeed, AppColors.patientAccent, Brightness.light);

  /// Thème patient sombre (mêmes tokens, luminosité inversée).
  static ThemeData patientDark() =>
      _base(AppColors.patientSeed, AppColors.patientAccent, Brightness.dark);

  /// Thème du portail hospitalier (contexte institutionnel).
  static ThemeData hospital() =>
      _base(AppColors.hospitalSeed, AppColors.hospitalSeed, Brightness.light);

  /// Thème neutre conservé pour compatibilité (équivaut au thème patient).
  static ThemeData light() => patient();

  /// Police serif de marque, embarquée en asset (aucun accès réseau).
  static const String _serifFamily = 'Gelasio';
  static const String _serifPackage = 'design_system';

  /// Applique la police serif de marque (Gelasio) aux styles de titre.
  ///
  /// La police est fournie en asset du design system : rendu identique hors
  /// ligne, sans téléchargement (essentiel en contexte d'urgence).
  static TextStyle _serif(TextStyle base) =>
      base.copyWith(fontFamily: _serifFamily, package: _serifPackage);

  static ThemeData _base(Color seed, Color accent, Brightness brightness) {
    final ColorScheme scheme = ColorScheme.fromSeed(
      seedColor: seed,
      brightness: brightness,
    ).copyWith(primary: accent, onPrimary: Colors.white);
    const RoundedRectangleBorder pillShape =
        RoundedRectangleBorder(borderRadius: AppRadius.button);
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      // Cibles tactiles confortables (accessibilité, usage sous stress).
      materialTapTargetSize: MaterialTapTargetSize.padded,
      visualDensity: VisualDensity.standard,
      textTheme: TextTheme(
        // Titres en serif de marque ; corps en police système (lisibilité).
        headlineSmall: _serif(AppTypography.headline),
        titleMedium: _serif(AppTypography.title),
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
