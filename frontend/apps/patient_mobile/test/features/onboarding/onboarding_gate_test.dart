import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/app/patient_app.dart';
import 'package:patient_mobile/core/consent/consent_store.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';

/// Faux stockage de consentement en mémoire.
class _InMemoryConsentStore implements ConsentStore {
  String? accepted;

  @override
  Future<String?> acceptedTermsVersion() async => accepted;

  @override
  Future<void> acceptTerms(String version) async => accepted = version;
}

class _FakeRepository implements OrientationRepository {
  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async =>
      const Cached.live([MedicalNeed(code: 'maternity', label: 'Maternité')]);

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  }) async =>
      const [];

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters() async => null;
}

class _FakeLocation implements LocationService {
  @override
  Future<UserPosition> currentPosition() async =>
      const UserPosition(latitude: 5.35, longitude: -4.0);

  @override
  Future<void> openSettings(LocationFailure failure) async {}
}

Widget _app(_InMemoryConsentStore store) {
  return ProviderScope(
    overrides: [
      consentStoreProvider.overrideWithValue(store),
      orientationRepositoryProvider.overrideWithValue(_FakeRepository()),
      locationServiceProvider.overrideWithValue(_FakeLocation()),
    ],
    child: const PatientApp(),
  );
}

void main() {
  testWidgets("un nouvel utilisateur voit l'accueil et les limites médicales",
      (tester) async {
    await tester.pumpWidget(_app(_InMemoryConsentStore()));
    await tester.pumpAndSettle();

    expect(find.text('Limites médicales'), findsOneWidget);
    expect(find.textContaining('185'), findsWidgets);
    expect(find.textContaining('position'), findsWidgets);
    expect(find.text('De quel soin avez-vous besoin ?'), findsNothing);
  });

  testWidgets("accepter persiste la version et ouvre le parcours",
      (tester) async {
    final store = _InMemoryConsentStore();
    await tester.pumpWidget(_app(store));
    await tester.pumpAndSettle();

    final accept = find.text("J'accepte les conditions et je continue");
    await tester.scrollUntilVisible(accept, 200,
        scrollable: find.byType(Scrollable).first);
    await tester.tap(accept);
    await tester.pumpAndSettle();

    expect(store.accepted, currentTermsVersion);
    expect(find.text('De quel soin avez-vous besoin ?'), findsOneWidget);
  });

  testWidgets('la version acceptée à jour ne redemande pas les conditions',
      (tester) async {
    final store = _InMemoryConsentStore()..accepted = currentTermsVersion;
    await tester.pumpWidget(_app(store));
    await tester.pumpAndSettle();

    expect(find.text('Limites médicales'), findsNothing);
    expect(find.text('De quel soin avez-vous besoin ?'), findsOneWidget);
  });

  testWidgets('une ANCIENNE version acceptée redemande les conditions',
      (tester) async {
    final store = _InMemoryConsentStore()..accepted = '2025-01-v0';
    await tester.pumpWidget(_app(store));
    await tester.pumpAndSettle();

    expect(find.text('Limites médicales'), findsOneWidget);
  });
}
