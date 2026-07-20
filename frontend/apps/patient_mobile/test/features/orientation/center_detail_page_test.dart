import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/core/navigation/navigation_launcher.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/center_detail_page.dart';

class _Caller implements EmergencyCaller {
  final List<String> calls = [];
  @override
  Future<void> call(String phoneNumber) async => calls.add(phoneNumber);
}

class _Nav implements NavigationLauncher {
  final List<String> targets = [];
  @override
  Future<void> navigateTo({
    required double latitude,
    required double longitude,
    required String label,
  }) async =>
      targets.add(label);
}

void main() {
  const center = RecommendedCenter(
    facilityId: 'id-1',
    name: 'CHU de Cocody',
    latitude: 5.3496,
    longitude: -3.9851,
    phone: '+2250100000001',
    distanceMeters: 1200,
    travelTimeSeconds: 360,
    travelTimeQuality: 'REAL',
    status: 'AVAILABLE',
    explanation: 'service disponible, proche de vous',
  );

  Widget host(RecommendedCenter c, _Caller caller, _Nav nav) {
    return ProviderScope(
      overrides: [
        emergencyCallerProvider.overrideWithValue(caller),
        navigationLauncherProvider.overrideWithValue(nav),
      ],
      child: MaterialApp(home: CenterDetailPage(center: c)),
    );
  }

  testWidgets('affiche nom, statut, distance et raison', (tester) async {
    await tester.pumpWidget(host(center, _Caller(), _Nav()));
    await tester.pump();

    expect(find.text('CHU de Cocody'), findsOneWidget);
    expect(find.text('Disponible'), findsOneWidget);
    expect(find.textContaining('1.2 km'), findsOneWidget);
    expect(find.text('service disponible, proche de vous'), findsOneWidget);
    expect(find.text('Y aller'), findsOneWidget);
  });

  testWidgets('les actions déclenchent itinéraire et appel', (tester) async {
    final caller = _Caller();
    final nav = _Nav();
    await tester.pumpWidget(host(center, caller, nav));
    await tester.pump();

    await tester.tap(find.text('Y aller'));
    await tester.tap(find.text('Appeler le centre'));
    expect(nav.targets, ['CHU de Cocody']);
    expect(caller.calls, ['+2250100000001']);
  });

  testWidgets('sans téléphone, l\'appel est désactivé', (tester) async {
    const noPhone = RecommendedCenter(
      facilityId: 'id-2',
      name: 'Centre sans numéro',
      latitude: 5.35,
      longitude: -4.0,
      distanceMeters: 3000,
      status: 'UNKNOWN',
      explanation: 'données locales',
    );
    await tester.pumpWidget(host(noPhone, _Caller(), _Nav()));
    await tester.pump();

    expect(find.text('Numéro indisponible'), findsOneWidget);
    final button = tester.widget<OutlinedButton>(find.byType(OutlinedButton));
    expect(button.onPressed, isNull);
  });

  testWidgets('n\'invente aucun service non fourni par le contrat',
      (tester) async {
    await tester.pumpWidget(host(center, _Caller(), _Nav()));
    await tester.pump();

    // La fiche ne doit pas afficher de section « services offerts » tant que la
    // donnée n'existe pas (honnêteté : jamais d'information non confirmée).
    expect(find.textContaining('Services'), findsNothing);
    expect(find.text('Chirurgie'), findsNothing);
    expect(find.text('Maternité'), findsNothing);
  });
}
