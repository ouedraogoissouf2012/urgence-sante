import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/position_map.dart';

/// Comportement de recentrage de la carte. On observe la caméra via un
/// `MapController` injecté, sans dépendre du réseau de tuiles OSM.
void main() {
  const cocody = RecommendedCenter(
    facilityId: 'id-1',
    name: 'CHU de Cocody',
    latitude: 5.3496,
    longitude: -3.9851,
    distanceMeters: 2800,
    status: 'AVAILABLE',
    explanation: 'service disponible',
  );
  const yopougon = RecommendedCenter(
    facilityId: 'id-2',
    name: 'CHU de Yopougon',
    latitude: 5.3363,
    longitude: -4.0894,
    distanceMeters: 6900,
    status: 'UNKNOWN',
    explanation: 'disponibilité non confirmée',
  );

  Widget host(MapController controller, {
    required List<RecommendedCenter> centers,
    required String? selectedCenterId,
    required int recenterSeq,
  }) {
    return MaterialApp(
      theme: AppTheme.patient(),
      home: Scaffold(
        body: PositionMap(
          latitude: 5.30,
          longitude: -4.00,
          centers: centers,
          selectedCenterId: selectedCenterId,
          recenterSeq: recenterSeq,
          controller: controller,
        ),
      ),
    );
  }

  bool isNear(LatLng a, double lat, double lon) =>
      (a.latitude - lat).abs() < 1e-6 && (a.longitude - lon).abs() < 1e-6;

  testWidgets('re-sélectionner le même centre (jeton incrémenté) recentre',
      (tester) async {
    final controller = MapController();

    // Sélection initiale sur Cocody (jeton = 1).
    await tester.pumpWidget(host(controller,
        centers: const [cocody, yopougon], selectedCenterId: 'id-1', recenterSeq: 1));
    await tester.pumpAndSettle();
    expect(isNear(controller.camera.center, cocody.latitude, cocody.longitude),
        isTrue,
        reason: 'la carte doit d\'abord se centrer sur le centre sélectionné');

    // L'utilisateur déplace la carte ailleurs, à la main.
    controller.move(const LatLng(4.0, -5.0), 13);
    expect(isNear(controller.camera.center, cocody.latitude, cocody.longitude),
        isFalse);

    // Re-tap sur le MÊME centre : selectedCenterId inchangé, mais jeton = 2.
    await tester.pumpWidget(host(controller,
        centers: const [cocody, yopougon], selectedCenterId: 'id-1', recenterSeq: 2));
    await tester.pumpAndSettle();

    expect(isNear(controller.camera.center, cocody.latitude, cocody.longitude),
        isTrue,
        reason: 'un nouveau jeton doit ramener la carte, même sur le même centre');
  });

  testWidgets('sélection avant l\'arrivée des données : recentrage rejoué',
      (tester) async {
    final controller = MapController();

    // Intention de recentrage émise (jeton = 1) alors qu'AUCUN centre n'est
    // encore chargé : la commande ne peut pas s'appliquer tout de suite.
    await tester.pumpWidget(host(controller,
        centers: const [], selectedCenterId: 'id-1', recenterSeq: 1));
    await tester.pumpAndSettle();
    expect(isNear(controller.camera.center, cocody.latitude, cocody.longitude),
        isFalse,
        reason: 'sans données, la caméra ne peut pas encore être sur le centre');

    // Les résultats arrivent (jeton inchangé) : la cible en attente est rejouée.
    await tester.pumpWidget(host(controller,
        centers: const [cocody, yopougon], selectedCenterId: 'id-1', recenterSeq: 1));
    await tester.pumpAndSettle();

    expect(isNear(controller.camera.center, cocody.latitude, cocody.longitude),
        isTrue,
        reason: 'le recentrage en attente doit se déclencher quand les centres arrivent');
  });
}
