import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:patient_mobile/app/patient_app.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/core/consent/consent_store.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/core/navigation/navigation_launcher.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';

/// Test E2E NAVIGATEUR (moteur web réel via integration_test) : parcours patient
/// complet — consentement → besoin → localisation → recommandations →
/// itinéraire/appel → appel d'urgence → panne réseau/hors ligne.
///
/// Exécution : depuis apps/patient_mobile
///   flutter test integration_test/parcours_e2e_test.dart -d chrome
class _Repo implements OrientationRepository {
  bool online = true;

  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async => const Cached.live([
        MedicalNeed(code: 'maternity', label: 'Maternité'),
        MedicalNeed(code: 'emergency', label: 'Urgences'),
      ]);

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
      : Cached.fromStore(const [
          RecommendedCenter(
            facilityId: 'id-1',
            name: 'CHU de Cocody',
            latitude: 5.3496,
            longitude: -3.9851,
            distanceMeters: 2800,
            status: 'UNKNOWN',
            explanation: 'données locales',
          ),
        ], syncedAt: DateTime.utc(2026, 7, 19, 10));
}

/// Store de consentement en mémoire : démarre NON accepté, l'acceptation via
/// l'UI le persiste et fait transiter l'écran (comme en production).
class _ConsentStore implements ConsentStore {
  String? accepted;
  @override
  Future<String?> acceptedTermsVersion() async => accepted;
  @override
  Future<void> acceptTerms(String version) async => accepted = version;
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
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('parcours patient complet dans le navigateur', (tester) async {
    final repo = _Repo();
    final caller = _Caller();
    final nav = _Nav();

    await tester.binding.setSurfaceSize(const Size(500, 1200));

    await tester.pumpWidget(ProviderScope(
      overrides: [
        orientationRepositoryProvider.overrideWithValue(repo),
        locationServiceProvider.overrideWithValue(_Location()),
        emergencyCallerProvider.overrideWithValue(caller),
        navigationLauncherProvider.overrideWithValue(nav),
        // Store en mémoire NON accepté : l'acceptation via l'UI fait transiter
        // l'écran (consentUpToDateProvider recalcule vrai), comme en production.
        consentStoreProvider.overrideWithValue(_ConsentStore()),
      ],
      child: const PatientApp(),
    ));
    await tester.pumpAndSettle();

    // 1) Accueil + consentement.
    expect(find.text('Limites médicales'), findsOneWidget);
    final accept = find.text("J'accepte les conditions et je continue");
    await tester.scrollUntilVisible(accept, 200,
        scrollable: find.byType(Scrollable).first);
    await tester.tap(accept);
    await tester.pumpAndSettle();

    // 2) Catalogue des besoins + appels d'urgence permanents.
    expect(find.text('De quel soin avez-vous besoin ?'), findsOneWidget);
    expect(find.text('SAMU 185'), findsOneWidget);
    expect(find.text('Pompiers 180'), findsOneWidget);

    // 3) Besoin → localisation → recommandation.
    await tester.tap(find.text('Maternité'));
    await tester.pumpAndSettle();
    expect(find.text('CHU de Cocody'), findsOneWidget);
    expect(find.text('Disponible'), findsOneWidget);

    // 4) Itinéraire + appel du centre.
    await tester.ensureVisible(find.text('Itinéraire'));
    await tester.tap(find.text('Itinéraire'));
    await tester.ensureVisible(find.text('Appeler'));
    await tester.tap(find.text('Appeler'));
    expect(nav.targets, ['CHU de Cocody']);
    expect(caller.calls, ['+2250100000001']);

    // 5) Appel d'urgence direct.
    await tester.tap(find.text('SAMU 185'));
    expect(caller.calls, contains('185'));

    // 6) Panne réseau → repli hors ligne.
    repo.online = false;
    await tester.tap(find.text('Maternité'));
    await tester.pumpAndSettle();
    expect(find.textContaining('Hors ligne'), findsOneWidget);
    expect(find.text('CHU de Cocody'), findsOneWidget);
  });
}
