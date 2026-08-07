import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_results_page.dart';
import 'package:patient_mobile/features/orientation/presentation/widgets/recommendation_card.dart';

class _FakeRepository implements OrientationRepository {
  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async =>
      const [];

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters() async => null;
}

class _DeniedLocation implements LocationService {
  @override
  Future<UserPosition> currentPosition() async => throw
      const LocationUnavailableException('Autorisation refusée.', LocationFailure.denied);

  @override
  Future<void> openSettings(LocationFailure failure) async {}
}

/// Localisation d'abord DÉSACTIVÉE (réglages), puis fonctionnelle : simule
/// l'utilisateur qui active la localisation dans les réglages et revient.
class _ServiceDisabledThenOkLocation implements LocationService {
  bool _firstCallDone = false;

  @override
  Future<UserPosition> currentPosition() async {
    if (!_firstCallDone) {
      _firstCallDone = true;
      throw const LocationUnavailableException(
          'La localisation est désactivée sur cet appareil.',
          LocationFailure.serviceDisabled);
    }
    return const UserPosition(latitude: 5.35, longitude: -4.0);
  }

  @override
  Future<void> openSettings(LocationFailure failure) async {}
}

class _RecordingCaller implements EmergencyCaller {
  final List<String> calls = [];

  @override
  Future<void> call(String phoneNumber) async => calls.add(phoneNumber);
}

/// Monte l'écran de RÉSULTATS (atteint depuis un symptôme) : il déclenche seul
/// la recherche multi-services au montage. La barre d'appel d'urgence y est
/// toujours présente.
Widget _resultsPage(_RecordingCaller caller, {LocationService? location}) {
  return ProviderScope(
    overrides: [
      orientationRepositoryProvider.overrideWithValue(_FakeRepository()),
      locationServiceProvider.overrideWithValue(location ?? _DeniedLocation()),
      emergencyCallerProvider.overrideWithValue(caller),
    ],
    child: const MaterialApp(
      home: OrientationResultsPage(serviceCodes: ['maternity'], title: 'Maternité'),
    ),
  );
}

void main() {
  testWidgets('les appels 185/180 sont visibles sur l\'écran de résultats',
      (tester) async {
    await tester.pumpWidget(_resultsPage(_RecordingCaller()));
    await tester.pumpAndSettle();

    expect(find.text('SAMU 185'), findsOneWidget);
    expect(find.text('Pompiers 180'), findsOneWidget);
  });

  testWidgets("le bouton SAMU déclenche l'appel du 185", (tester) async {
    final caller = _RecordingCaller();
    await tester.pumpWidget(_resultsPage(caller));
    await tester.pumpAndSettle();

    await tester.tap(find.text('SAMU 185'));

    expect(caller.calls, ['185']);
  });

  testWidgets("une localisation refusée affiche l'erreur et le réessai",
      (tester) async {
    // La recherche se déclenche au montage → localisation refusée → erreur.
    await tester.pumpWidget(_resultsPage(_RecordingCaller()));
    await tester.pumpAndSettle();

    expect(find.textContaining('Autorisation refusée'), findsOneWidget);
    expect(find.text('Réessayer'), findsOneWidget);
    // Les appels d'urgence restent accessibles pendant l'erreur.
    expect(find.text('SAMU 185'), findsOneWidget);
  });

  testWidgets(
      'localisation désactivée : « Réessayer » est proposé À CÔTÉ des réglages',
      (tester) async {
    // Sans ce bouton, l'utilisateur revenu des réglages restait bloqué sur
    // l'écran d'erreur (constaté sur appareil réel).
    await tester.pumpWidget(
        _resultsPage(_RecordingCaller(), location: _ServiceDisabledThenOkLocation()));
    await tester.pumpAndSettle();

    expect(find.text('Ouvrir les réglages'), findsOneWidget);
    expect(find.text('Réessayer'), findsOneWidget);
  });

  testWidgets(
      'au retour des réglages, la localisation est re-tentée automatiquement',
      (tester) async {
    await tester.pumpWidget(
        _resultsPage(_RecordingCaller(), location: _ServiceDisabledThenOkLocation()));
    await tester.pumpAndSettle();
    expect(find.text('Ouvrir les réglages'), findsOneWidget);

    // L'utilisateur active la localisation dans les réglages puis REVIENT
    // dans l'application (cycle de vie : resumed).
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
    await tester.pumpAndSettle();

    expect(find.text('Ouvrir les réglages'), findsNothing,
        reason: 'au retour, l\'app doit re-tenter seule et quitter l\'erreur');
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

  testWidgets('un temps ESTIMATED (mode dégradé) est signalé', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: RecommendationCard(
            center: RecommendedCenter(
              facilityId: 'id-3',
              name: 'Centre dégradé',
              latitude: 5.30,
              longitude: -4.00,
              distanceMeters: 3400,
              travelTimeQuality: 'ESTIMATED',
              status: 'UNKNOWN',
              explanation: 'temps non fourni par le routage',
            ),
          ),
        ),
      ),
    );

    expect(find.textContaining('temps estimé'), findsOneWidget);
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
