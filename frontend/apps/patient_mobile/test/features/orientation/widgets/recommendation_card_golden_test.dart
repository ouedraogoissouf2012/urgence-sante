import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/recommendation_card.dart';

/// Golden ciblé (issue #47) : verrouille le rendu de la fiche de recommandation
/// (mise en page, badge de statut, actions) contre les régressions visuelles.
/// Régénérer avec `flutter test --update-goldens` après un changement voulu.
void main() {
  testWidgets('fiche de recommandation — rendu de référence', (tester) async {
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
