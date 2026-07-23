import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:testing_support/testing_support.dart';

/// Vérifie le calcul de contraste lui-même contre des valeurs WCAG connues :
/// une garde d'accessibilité ne vaut que si sa formule est juste.
void main() {
  group('WcagContrast.ratio', () {
    test('noir sur blanc = 21:1 (contraste maximal)', () {
      expect(WcagContrast.ratio(Colors.black, Colors.white),
          closeTo(21.0, 0.01));
    });

    test('une couleur avec elle-même = 1:1', () {
      expect(WcagContrast.ratio(Colors.white, Colors.white),
          closeTo(1.0, 0.001));
    });

    test('symétrique (l\'ordre des arguments est indifférent)', () {
      const a = Color(0xFFB00020);
      const b = Color(0xFFFFF8F7);
      expect(WcagContrast.ratio(a, b), WcagContrast.ratio(b, a));
    });

    test('valeur de référence : #767676 sur blanc ≈ 4.54:1 (seuil AA)', () {
      // Gris de référence WebAIM, tout juste conforme AA texte normal.
      expect(WcagContrast.ratio(const Color(0xFF767676), Colors.white),
          closeTo(4.54, 0.05));
    });
  });

  group('WcagContrast.composite', () {
    test('alpha = 1 renvoie le premier plan intact', () {
      const fg = Color(0xFF123456);
      final result = WcagContrast.composite(fg, 1, Colors.white);
      expect(result.r, closeTo(fg.r, 0.001));
      expect(result.g, closeTo(fg.g, 0.001));
      expect(result.b, closeTo(fg.b, 0.001));
    });

    test('alpha = 0 renvoie le fond intact', () {
      final result = WcagContrast.composite(Colors.black, 0, Colors.white);
      expect(WcagContrast.ratio(result, Colors.white), closeTo(1.0, 0.001));
    });

    test('un fond translucide réduit le contraste perçu', () {
      // Texte ambre vif sur fond blanc : illisible en direct, mais le fond
      // réel du badge (ambre à 12 % sur blanc) est ce qu'on doit mesurer.
      const ambre = Color(0xFFF9A825);
      final badgeBg = WcagContrast.composite(ambre, 0.12, Colors.white);
      // Le fond composé reste très clair → le texte ambre dessus est illisible.
      expect(WcagContrast.ratio(ambre, badgeBg),
          lessThan(WcagContrast.aaNormalText));
    });
  });
}
