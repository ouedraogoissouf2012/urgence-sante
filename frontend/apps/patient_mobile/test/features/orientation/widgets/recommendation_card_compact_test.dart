import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/recommendation_card_compact.dart';

/// Robustesse de la fiche compacte (centres secondaires) : petits écrans,
/// grandes polices, nom très long — sans débordement, contenu essentiel visible.
void main() {
  const center = RecommendedCenter(
    facilityId: 'id-2',
    name: "Hôpital Général d'Abobo — Service des urgences générales",
    latitude: 5.3496,
    longitude: -3.9851,
    distanceMeters: 6900,
    travelTimeSeconds: 540,
    status: 'LIMITED',
    explanation: 'disponibilité limitée',
  );

  Widget host({required double width, double textScale = 1.0}) {
    return MediaQuery(
      data: MediaQueryData(textScaler: TextScaler.linear(textScale)),
      child: MaterialApp(
        theme: AppTheme.patient(),
        home: Scaffold(
          body: Center(
            child: SizedBox(
              width: width,
              child: const SingleChildScrollView(
                child: RecommendationCardCompact(center: center, rank: 2),
              ),
            ),
          ),
        ),
      ),
    );
  }

  for (final scenario in const [
    (name: 'petit écran', width: 320.0, scale: 1.0),
    (name: 'grande police', width: 360.0, scale: 2.0),
    (name: 'très grande police', width: 360.0, scale: 3.0),
    (name: 'desktop large', width: 1200.0, scale: 1.0),
  ]) {
    testWidgets('aucun overflow — ${scenario.name}', (tester) async {
      await tester.pumpWidget(
          host(width: scenario.width, textScale: scenario.scale));
      await tester.pump();

      // Un débordement de rendu lève une exception captée ici.
      expect(tester.takeException(), isNull);
    });
  }

  testWidgets('affiche le rang, le statut et la distance', (tester) async {
    await tester.pumpWidget(host(width: 400));

    expect(find.text('2'), findsOneWidget);
    expect(find.text('Limité'), findsOneWidget);
    expect(find.textContaining('6.9 km'), findsOneWidget);
  });

  testWidgets('un appui remonte la sélection', (tester) async {
    RecommendedCenter? tapped;
    await tester.pumpWidget(MaterialApp(
      theme: AppTheme.patient(),
      home: Scaffold(
        body: RecommendationCardCompact(
          center: center,
          rank: 2,
          onTap: () => tapped = center,
        ),
      ),
    ));

    await tester.tap(find.byType(RecommendationCardCompact));
    expect(tapped, center);
  });
}
