import 'dart:math' as math;

import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  Future<void> pump(WidgetTester tester, Widget child) {
    return tester.pumpWidget(MaterialApp(home: Scaffold(body: child)));
  }

  testWidgets("l'information est portée par le texte, pas la couleur seule",
      (tester) async {
    await pump(tester, const StatusBadge(kind: StatusBadgeKind.available));

    expect(find.text('Disponible'), findsOneWidget);
  });

  testWidgets('fromApi mappe les statuts du contrat', (tester) async {
    await pump(tester, StatusBadge.fromApi('SATURATED'));

    expect(find.text('Saturé'), findsOneWidget);
  });

  testWidgets('un statut inconnu est affiché comme non confirmé', (tester) async {
    await pump(tester, StatusBadge.fromApi('WEIRD_VALUE'));

    expect(find.text('Non confirmé'), findsOneWidget);
  });

  // ── Garde d'accessibilité : lisibilité du texte du badge ─────────────────
  // Le libellé porte l'information de disponibilité ; s'il n'est pas lisible,
  // la promesse d'accessibilité du design system est rompue. On mesure le
  // contraste WCAG sur le fond RÉEL du badge (teinte de statut à 12 % sur la
  // surface), pas sur blanc pur — et sur les DEUX thèmes : une couleur de texte
  // foncée lisible en clair devient illisible en sombre et inversement.
  group('contraste du texte (AA ≥ 4.5:1 sur le fond du badge)', () {
    double channel(double c) => c <= 0.03928
        ? c / 12.92
        : math.pow((c + 0.055) / 1.055, 2.4).toDouble();

    double luminance(Color color) =>
        0.2126 * channel(color.r) +
        0.7152 * channel(color.g) +
        0.0722 * channel(color.b);

    double contrast(Color a, Color b) {
      final double la = luminance(a);
      final double lb = luminance(b);
      return (math.max(la, lb) + 0.05) / (math.min(la, lb) + 0.05);
    }

    // Premier plan translucide composité sur un fond opaque (alpha blending).
    Color composite(Color fg, double alpha, Color bg) => Color.from(
          alpha: 1,
          red: fg.r * alpha + bg.r * (1 - alpha),
          green: fg.g * alpha + bg.g * (1 - alpha),
          blue: fg.b * alpha + bg.b * (1 - alpha),
        );

    // Doit rester aligné sur StatusBadge (opacité du fond). Surfaces réelles
    // Material 3 mesurées sur les thèmes patient() / patientDark().
    const double badgeFillAlpha = 0.12;
    const Color lightSurface = Color(0xFFFFF8F7);
    const Color darkSurface = Color(0xFF1A1110);

    // (nom, teinte, texte clair, texte sombre).
    const List<(String, Color, Color, Color)> statuses = [
      (
        'Disponible',
        AppColors.statusAvailable,
        AppColors.statusAvailableTextLight,
        AppColors.statusAvailableTextDark,
      ),
      (
        'Limité',
        AppColors.statusLimited,
        AppColors.statusLimitedTextLight,
        AppColors.statusLimitedTextDark,
      ),
      (
        'Saturé',
        AppColors.statusSaturated,
        AppColors.statusSaturatedTextLight,
        AppColors.statusSaturatedTextDark,
      ),
      (
        'Fermé',
        AppColors.statusClosed,
        AppColors.statusClosedTextLight,
        AppColors.statusClosedTextDark,
      ),
      (
        'Non confirmé',
        AppColors.statusUnknown,
        AppColors.statusUnknownTextLight,
        AppColors.statusUnknownTextDark,
      ),
    ];

    for (final (name, tint, textLight, textDark) in statuses) {
      test('$name lisible en thème clair', () {
        final Color bg = composite(tint, badgeFillAlpha, lightSurface);
        final double ratio = contrast(textLight, bg);
        expect(ratio, greaterThanOrEqualTo(4.5),
            reason: '$name (clair) : ${ratio.toStringAsFixed(2)}:1 < 4.5:1');
      });

      test('$name lisible en thème sombre', () {
        final Color bg = composite(tint, badgeFillAlpha, darkSurface);
        final double ratio = contrast(textDark, bg);
        expect(ratio, greaterThanOrEqualTo(4.5),
            reason: '$name (sombre) : ${ratio.toStringAsFixed(2)}:1 < 4.5:1');
      });
    }
  });
}
