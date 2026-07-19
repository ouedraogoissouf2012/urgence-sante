import 'dart:async';
import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' show Size;

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/app/patient_app.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/core/navigation/navigation_launcher.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';

/// Parcours patient de bout en bout (issue #48), côté interface :
/// besoin → localisation → recommandation → itinéraire/appel → panne réseau.
class _Repo implements OrientationRepository {
  bool online = true;

  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async =>
      const Cached.live([MedicalNeed(code: 'maternity', label: 'Maternité')]);

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
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
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters() async => online
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

/// Renvoie une image PNG 1×1 transparente pour toute requête HTTP (tuiles OSM).
class _FakeHttpOverrides extends HttpOverrides {
  @override
  HttpClient createHttpClient(SecurityContext? context) => _FakeHttpClient();
}

// PNG transparent 1×1 minimal.
final Uint8List _transparentPng = Uint8List.fromList(<int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
  0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4, 0x89, 0x00, 0x00, 0x00,
  0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49,
  0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
]);

class _FakeHttpClient implements HttpClient {
  @override
  Future<HttpClientRequest> getUrl(Uri url) async => _FakeHttpClientRequest();

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

class _FakeHttpClientRequest implements HttpClientRequest {
  @override
  final HttpHeaders headers = _FakeHttpHeaders();

  @override
  Future<HttpClientResponse> close() async => _FakeHttpClientResponse();

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

class _FakeHttpClientResponse implements HttpClientResponse {
  @override
  int get statusCode => 200;

  @override
  int get contentLength => _transparentPng.length;

  @override
  HttpClientResponseCompressionState get compressionState =>
      HttpClientResponseCompressionState.notCompressed;

  @override
  StreamSubscription<List<int>> listen(
    void Function(List<int> event)? onData, {
    Function? onError,
    void Function()? onDone,
    bool? cancelOnError,
  }) {
    return Stream<List<int>>.fromIterable(<List<int>>[_transparentPng]).listen(
      onData,
      onError: onError,
      onDone: onDone,
      cancelOnError: cancelOnError,
    );
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

class _FakeHttpHeaders implements HttpHeaders {
  @override
  dynamic noSuchMethod(Invocation invocation) => null;
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
  setUpAll(() => HttpOverrides.global = _FakeHttpOverrides());
  tearDownAll(() => HttpOverrides.global = null);

  testWidgets('parcours complet : besoin → reco → itinéraire/appel → hors ligne',
      (tester) async {
    final repo = _Repo();
    final caller = _Caller();
    final nav = _Nav();

    await tester.binding.setSurfaceSize(const Size(500, 1200));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(ProviderScope(
      overrides: [
        orientationRepositoryProvider.overrideWithValue(repo),
        locationServiceProvider.overrideWithValue(_Location()),
        emergencyCallerProvider.overrideWithValue(caller),
        navigationLauncherProvider.overrideWithValue(nav),
        consentUpToDateProvider.overrideWith((ref) async => true),
      ],
      child: const PatientApp(),
    ));
    await tester.pumpAndSettle();

    // 1) Les appels d'urgence sont visibles d'emblée.
    expect(find.text('SAMU 185'), findsOneWidget);
    expect(find.text('Pompiers 180'), findsOneWidget);

    // 2) Choix du besoin → localisation automatique → recommandation.
    await tester.tap(find.text('Maternité'));
    await tester.pumpAndSettle();
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

    // 5) Panne réseau : repli hors ligne sur les derniers centres connus.
    repo.online = false;
    await tester.tap(find.text('Maternité'));
    await tester.pumpAndSettle();
    expect(find.textContaining('Hors ligne'), findsOneWidget);
    expect(find.text('CHU de Cocody'), findsOneWidget);
  });
}
