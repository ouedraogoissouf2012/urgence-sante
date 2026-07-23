import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:testing_support/testing_support.dart';

/// Garde de contraste transverse : chaque couple (texte, surface) réellement
/// affiché par l'interface doit atteindre le seuil WCAG AA dans **les deux
/// thèmes** (clair et sombre).
///
/// Pourquoi cette garde existe : deux régressions successives ont livré du
/// texte illisible en mode sombre — d'abord les libellés de badge, ensuite la
/// distance de la carte de recommandation (accent rouge sur surface teintée,
/// ~1.7:1). À chaque fois, les tests passaient : le mode sombre était un angle
/// mort. Cette garde le supprime en vérifiant les combinaisons sémantiques du
/// design system sur les surfaces **réelles** des thèmes (lues via
/// `ColorScheme`, pas codées en dur), pour que ni un changement de token ni une
/// évolution de la génération Material ne puissent réintroduire le défaut.
void main() {
  // Opacité du fond translucide du badge — alignée sur `StatusBadge`.
  const double badgeFillAlpha = 0.12;

  /// Un couple à vérifier : nom lisible, fonction qui donne la couleur de texte
  /// et la surface (opaque) sur laquelle elle est posée, pour un `ColorScheme`.
  /// La surface peut être composée (badge translucide) ou directe (onSurface).
  ({String name, Color text, Color surface}) pair(
    String name,
    Color text,
    Color surface,
  ) =>
      (name: name, text: text, surface: surface);

  /// Construit la liste des couples à garder pour un thème donné.
  /// `isDark` sélectionne la variante de texte des tokens de statut (fixes),
  /// tandis que les surfaces et les couleurs `on…` proviennent du scheme réel.
  List<({String name, Color text, Color surface})> pairsFor(
    ColorScheme scheme,
    bool isDark,
  ) {
    // Fond réel d'un badge : teinte du statut à 12 % sur la surface du thème.
    Color badgeBg(Color tint) =>
        WcagContrast.composite(tint, badgeFillAlpha, scheme.surface);

    Color statusText(Color light, Color dark) => isDark ? dark : light;

    return [
      // Badges de statut : texte sur fond teinté translucide.
      pair(
        'Badge Disponible',
        statusText(AppColors.statusAvailableTextLight,
            AppColors.statusAvailableTextDark),
        badgeBg(AppColors.statusAvailable),
      ),
      pair(
        'Badge Limité',
        statusText(
            AppColors.statusLimitedTextLight, AppColors.statusLimitedTextDark),
        badgeBg(AppColors.statusLimited),
      ),
      pair(
        'Badge Saturé',
        statusText(AppColors.statusSaturatedTextLight,
            AppColors.statusSaturatedTextDark),
        badgeBg(AppColors.statusSaturated),
      ),
      pair(
        'Badge Fermé',
        statusText(
            AppColors.statusClosedTextLight, AppColors.statusClosedTextDark),
        badgeBg(AppColors.statusClosed),
      ),
      pair(
        'Badge Non confirmé',
        statusText(
            AppColors.statusUnknownTextLight, AppColors.statusUnknownTextDark),
        badgeBg(AppColors.statusUnknown),
      ),
      // Carte de recommandation (fond surfaceContainerHighest).
      // Distance : texte principal — la régression #74 est née ici.
      pair('Carte — distance (onSurface)', scheme.onSurface,
          scheme.surfaceContainerHighest),
      // Raison : texte secondaire.
      pair('Carte — raison (onSurfaceVariant)', scheme.onSurfaceVariant,
          scheme.surfaceContainerHighest),
      // Bouton d'appel d'urgence : texte blanc sur fond rouge secours.
      pair('Bouton urgence (blanc/secours)', Colors.white,
          AppColors.emergencyCall),
      // Boutons remplis d'action : texte blanc sur l'accent primaire.
      pair('Bouton primaire (onPrimary/primary)', scheme.onPrimary,
          scheme.primary),
    ];
  }

  for (final (themeName, isDark, theme) in [
    ('clair', false, AppTheme.patient()),
    ('sombre', true, AppTheme.patientDark()),
  ]) {
    group('thème $themeName — texte lisible (WCAG AA)', () {
      final ColorScheme scheme = theme.colorScheme;
      for (final p in pairsFor(scheme, isDark)) {
        test(p.name, () {
          final double ratio = WcagContrast.ratio(p.text, p.surface);
          expect(
            ratio,
            greaterThanOrEqualTo(WcagContrast.aaNormalText),
            reason: '${p.name} ($themeName) : ${ratio.toStringAsFixed(2)}:1 '
                '< ${WcagContrast.aaNormalText}:1 (texte illisible)',
          );
        });
      }
    });
  }
}
