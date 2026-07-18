import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/app/patient_app.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/recommendation_card.dart';

class _FakeRepository implements OrientationRepository {
  @override
  Future<List<MedicalNeed>> loadNeeds() async =>
      const [MedicalNeed(code: 'maternity', label: 'Maternité')];

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  }) async =>
      const [];
}

class _DeniedLocation implements LocationService {
  @override
  Future<UserPosition> currentPosition() async => throw
      const LocationUnavailableException('Autorisation refusée.', LocationFailure.denied);

  @override
  Future<void> openSettings(LocationFailure failure) async {}
}

class _RecordingCaller implements EmergencyCaller {
  final List<String> calls = [];

  @override
  Future<void> call(String phoneNumber) async => calls.add(phoneNumber);
}

Widget _app(_RecordingCaller caller) {
  return ProviderScope(
    overrides: [
      orientationRepositoryProvider.overrideWithValue(_FakeRepository()),
      locationServiceProvider.overrideWithValue(_DeniedLocation()),
      emergencyCallerProvider.overrideWithValue(caller),
      // Conditions déjà acceptées : ces tests ciblent le parcours principal.
      consentUpToDateProvider.overrideWith((ref) async => true),
    ],
    child: const PatientApp(),
  );
}

void main() {
  testWidgets('le catalogue des besoins et les appels 185/180 sont visibles',
      (tester) async {
    await tester.pumpWidget(_app(_RecordingCaller()));
    await tester.pumpAndSettle();

    expect(find.text('Maternité'), findsOneWidget);
    expect(find.text('SAMU 185'), findsOneWidget);
    expect(find.text('Pompiers 180'), findsOneWidget);
  });

  testWidgets("le bouton SAMU déclenche l'appel du 185", (tester) async {
    final caller = _RecordingCaller();
    await tester.pumpWidget(_app(caller));
    await tester.pumpAndSettle();

    await tester.tap(find.text('SAMU 185'));

    expect(caller.calls, ['185']);
  });

  testWidgets("une localisation refusée affiche l'erreur et le réessai",
      (tester) async {
    await tester.pumpWidget(_app(_RecordingCaller()));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Maternité'));
    await tester.pumpAndSettle();

    expect(find.textContaining('Autorisation refusée'), findsOneWidget);
    expect(find.text('Réessayer'), findsOneWidget);
    // Les appels d'urgence restent accessibles pendant l'erreur.
    expect(find.text('SAMU 185'), findsOneWidget);
  });

  const sampleCenter = RecommendedCenter(
    facilityId: 'id-1',
    name: 'CHU de Cocody',
    latitude: 5.3496,
    longitude: -3.9851,
    phone: '+2250100000001',
    distanceMeters: 2800,
    travelTimeSeconds: 320,
    status: 'AVAILABLE',
    explanation: 'service disponible · à 2.8 km (~5 min)',
  );

  testWidgets('la fiche montre statut, distance, raison et actions',
      (tester) async {
    var called = false;
    var navigated = false;
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: RecommendationCard(
            center: sampleCenter,
            onCall: () => called = true,
            onNavigate: () => navigated = true,
          ),
        ),
      ),
    );

    expect(find.text('CHU de Cocody'), findsOneWidget);
    expect(find.text('Disponible'), findsOneWidget);
    expect(find.text('2.8 km · ~5 min'), findsOneWidget);
    expect(find.textContaining('service disponible'), findsOneWidget);

    await tester.tap(find.text('Appeler'));
    await tester.tap(find.text('Itinéraire'));
    expect(called, isTrue);
    expect(navigated, isTrue);
  });

  testWidgets("sans téléphone connu, l'action Appeler est absente",
      (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: RecommendationCard(
            center: RecommendedCenter(
              facilityId: 'id-2',
              name: 'Centre sans téléphone',
              latitude: 5.30,
              longitude: -4.00,
              distanceMeters: 1200,
              status: 'UNKNOWN',
              explanation: 'disponibilité non confirmée',
            ),
          ),
        ),
      ),
    );

    expect(find.text('Appeler'), findsNothing);
    expect(find.text('Itinéraire'), findsOneWidget);
  });
}
