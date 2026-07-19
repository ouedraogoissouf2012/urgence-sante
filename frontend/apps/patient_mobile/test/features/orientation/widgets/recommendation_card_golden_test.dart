import 'package:design_system/design_system.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/recommendation_card.dart';

/// Golden ciblé (issue #47) : verrouille le rendu de la fiche de recommandation
/// (mise en page, badge de statut, actions) contre les régressions visuelles.
/// Régénérer avec `flutter test --update-goldens` après un changement voulu.
///
/// La comparaison TOLÈRE un léger écart de rendu entre plateformes (Windows en
/// local ↔ Linux en CI, anti-crénelage des polices différent) : un écart de
/// quelques dixièmes de pour cent n'est pas une régression, un écart notable
/// échoue toujours.
class _TolerantGoldenComparator extends LocalFileComparator {
  _TolerantGoldenComparator(super.testFile);

  static const double _tolerance = 0.02;

  @override
  Future<bool> compare(Uint8List imageBytes, Uri golden) async {
    final ComparisonResult result = await GoldenFileComparator.compareLists(
      imageBytes,
      await getGoldenBytes(golden),
    );
    if (result.passed || result.diffPercent <= _tolerance) {
      return true;
    }
    throw FlutterError(await generateFailureOutput(result, golden, basedir));
  }
}

void main() {
  testWidgets('fiche de recommandation — rendu de référence', (tester) async {
    // basedir courante (dossier de CE fichier de test) → résolution correcte
    // du chemin du golden, avec tolérance inter-plateformes.
    final Uri testFile = (goldenFileComparator as LocalFileComparator).basedir;
    goldenFileComparator = _TolerantGoldenComparator(
      Uri.parse('${testFile}recommendation_card_golden_test.dart'),
    );

    await tester.binding.setSurfaceSize(const Size(400, 320));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(
      MaterialApp(
        theme: AppTheme.patient(),
        home: const Scaffold(
          body: Padding(
            padding: EdgeInsets.all(AppSpacing.md),
            child: RecommendationCard(
              center: RecommendedCenter(
                facilityId: 'id-1',
                name: 'CHU de Cocody',
                latitude: 5.3496,
                longitude: -3.9851,
                phone: '+2250100000001',
                distanceMeters: 2800,
                travelTimeSeconds: 320,
                status: 'AVAILABLE',
                explanation: 'service disponible',
              ),
            ),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await expectLater(
      find.byType(RecommendationCard),
      matchesGoldenFile('goldens/recommendation_card.png'),
    );
  });
}
