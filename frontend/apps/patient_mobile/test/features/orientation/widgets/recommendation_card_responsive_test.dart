import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/recommendation_card.dart';

/// Robustesse d'affichage de la fiche (issue #47) : petits écrans, grandes
/// polices et desktop, sans débordement, et actions accessibles.
void main() {
  const center = RecommendedCenter(
    facilityId: 'id-1',
    name: 'Centre Hospitalier Universitaire de Cocody — Maternité',
    latitude: 5.3496,
    longitude: -3.9851,
    phone: '+2250100000001',
    distanceMeters: 2800,
    travelTimeSeconds: 320,
    status: 'AVAILABLE',
    explanation: 'service disponible, mis à jour récemment',
  );

  Widget host({required double width, double textScale = 1.0}) {
    return MediaQuery(
      data: MediaQueryData(textScaler: TextScaler.linear(textScale)),
      child: MaterialApp(
        home: Scaffold(
          body: Center(
            child: SizedBox(
              width: width,
              child: const SingleChildScrollView(
                child: RecommendationCard(center: center),
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
      await tester.pumpWidget(host(width: scenario.width, textScale: scenario.scale));
      await tester.pump();

      // Un débordement de rendu lève une exception captée ici.
      expect(tester.takeException(), isNull);
      expect(find.text('Itinéraire'), findsOneWidget);
    });
  }

  testWidgets('les actions critiques annoncent le centre aux lecteurs d\'écran',
      (tester) async {
    await tester.pumpWidget(host(width: 400));

    expect(
      find.bySemanticsLabel(RegExp('Itinéraire vers .*Cocody')),
      findsOneWidget,
    );
    expect(
      find.bySemanticsLabel(RegExp('Appeler .*Cocody')),
      findsOneWidget,
    );
  });
}
