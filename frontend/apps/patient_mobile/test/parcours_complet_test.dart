import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/core/navigation/navigation_launcher.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_results_page.dart';

import 'support/fake_tile_http_overrides.dart';

/// Parcours patient de bout en bout (issue #48), côté interface :
/// besoin → localisation → recommandation → itinéraire/appel → panne réseau.
class _Repo implements OrientationRepository {
  bool online = true;

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async {
    if (!online) throw Exception('réseau indisponible');
    return const [
      RecommendedCenter(
        facilityId: 'id-1',
        name: 'CHU de Cocody',
        latitude: 5.3496,
        longitude: -3.9851,
        phone: '+2250100000001',
        distanceMeters: 2800,
        travelTimeSeconds: 320,
        travelTimeQuality: 'REAL',
        status: 'AVAILABLE',
        explanation: 'service disponible',
      ),
    ];
  }

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters({
    required List<String> serviceCodes,
  }) async =>
      online
      ? null
      : Cached.fromStore(
          const [
            RecommendedCenter(
              facilityId: 'id-1',
              name: 'CHU de Cocody',
              latitude: 5.3496,
              longitude: -3.9851,
              distanceMeters: 2800,
              status: 'UNKNOWN',
              explanation: 'données locales, disponibilité non confirmée',
            ),
          ],
          syncedAt: DateTime.utc(2026, 7, 19, 10),
        );
}

/// Repository qui nomme le centre d'après le besoin demandé : permet de vérifier
/// que deux pages de catégories différentes n'échangent PAS leurs résultats
/// (chaque page doit posséder son propre état d'orientation).
class _NeedAwareRepo implements OrientationRepository {
  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async =>
      [
        RecommendedCenter(
          facilityId: 'id-${serviceCodes.first}',
          name: 'Centre ${serviceCodes.first}',
          latitude: 5.3496,
          longitude: -3.9851,
          phone: '+2250100000001',
          distanceMeters: 2800,
          travelTimeSeconds: 320,
          travelTimeQuality: 'REAL',
          status: 'AVAILABLE',
          explanation: 'service disponible',
        ),
      ];

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters({
    required List<String> serviceCodes,
  }) async =>
      null;
}

class _Location implements LocationService {
  @override
  Future<UserPosition> currentPosition() async =>
      const UserPosition(latitude: 5.35, longitude: -4.02);

  @override
  Future<void> openSettings(LocationFailure failure) async {}
}

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
  // La carte OSM charge des tuiles réseau : en test, on renvoie une image
  // 1×1 transparente pour toute requête (aucun accès réseau réel).
  setUpAll(() => HttpOverrides.global = FakeTileHttpOverrides());
  tearDownAll(() => HttpOverrides.global = null);

  // Écran de RÉSULTATS atteint depuis un symptôme : il déclenche seul la
  // recherche multi-services au montage (les portes et le sélecteur à deux
  // niveaux sont testés séparément).
  Widget resultsPage(_Repo repo, _Caller caller, _Nav nav) => ProviderScope(
        overrides: [
          orientationRepositoryProvider.overrideWithValue(repo),
          locationServiceProvider.overrideWithValue(_Location()),
          emergencyCallerProvider.overrideWithValue(caller),
          navigationLauncherProvider.overrideWithValue(nav),
        ],
        child: const MaterialApp(
          home: OrientationResultsPage(
              serviceCodes: ['maternity'], title: 'Maternité'),
        ),
      );

  testWidgets('parcours : recherche → reco → itinéraire/appel → urgence',
      (tester) async {
    final repo = _Repo();
    final caller = _Caller();
    final nav = _Nav();

    await tester.binding.setSurfaceSize(const Size(500, 1200));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(resultsPage(repo, caller, nav));
    await tester.pumpAndSettle();

    // 1) Les appels d'urgence sont visibles.
    expect(find.text('SAMU 185'), findsOneWidget);
    expect(find.text('Pompiers 180'), findsOneWidget);

    // 2) La recherche s'est déclenchée seule → recommandation affichée.
    expect(find.text('CHU de Cocody'), findsOneWidget);
    expect(find.text('Disponible'), findsOneWidget);

    // 3) Itinéraire et appel du centre (défilement pour amener les actions).
    await tester.ensureVisible(find.text('Itinéraire'));
    await tester.tap(find.text('Itinéraire'));
    await tester.ensureVisible(find.text('Appeler'));
    await tester.tap(find.text('Appeler'));
    expect(nav.targets, ['CHU de Cocody']);
    expect(caller.calls, ['+2250100000001']);

    // 4) Appel d'urgence direct.
    await tester.tap(find.text('SAMU 185'));
    expect(caller.calls, contains('185'));
  });

  testWidgets('panne réseau : repli hors ligne sur les derniers centres connus',
      (tester) async {
    final repo = _Repo()..online = false;

    await tester.binding.setSurfaceSize(const Size(500, 1200));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(resultsPage(repo, _Caller(), _Nav()));
    await tester.pumpAndSettle();

    // La recherche échoue (réseau) → repli sur le cache : centres connus,
    // signalés hors ligne, sans statut temps réel.
    expect(find.textContaining('Hors ligne'), findsOneWidget);
    expect(find.text('CHU de Cocody'), findsOneWidget);
  });

  testWidgets(
      'isolation par catégorie : deux pages n\'échangent pas leurs centres (#108)',
      (tester) async {
    // Deux catégories affichées simultanément (comme deux pages empilées) : avec
    // un ViewModel global partagé, la seconde recherche écraserait la première
    // et les deux montreraient le même centre. Chaque page ayant son propre état,
    // chacune affiche SON centre.
    await tester.binding.setSurfaceSize(const Size(500, 1600));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(ProviderScope(
      overrides: [
        orientationRepositoryProvider.overrideWithValue(_NeedAwareRepo()),
        locationServiceProvider.overrideWithValue(_Location()),
        emergencyCallerProvider.overrideWithValue(_Caller()),
        navigationLauncherProvider.overrideWithValue(_Nav()),
      ],
      child: const MaterialApp(
        home: Column(
          children: [
            Expanded(
              child: OrientationResultsPage(
                  serviceCodes: ['maternity'], title: 'Maternité'),
            ),
            Expanded(
              child: OrientationResultsPage(
                  serviceCodes: ['cardiology'], title: 'Cardiologie'),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Centre maternity'), findsOneWidget);
    expect(find.text('Centre cardiology'), findsOneWidget);
  });
}
