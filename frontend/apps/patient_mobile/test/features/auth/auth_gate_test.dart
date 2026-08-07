import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/app/patient_app.dart';
import 'package:patient_mobile/core/consent/consent_store.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/auth/domain/auth_repository.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/model/emergency_category.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/repository/emergency_taxonomy_repository.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';

import 'support/auth_fakes.dart';

class _FakeRepository implements OrientationRepository {
  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async =>
      const Cached.live([MedicalNeed(code: 'maternity', label: 'Maternité')]);

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

class _FakeTaxonomy implements EmergencyTaxonomyRepository {
  @override
  Future<List<EmergencyCategory>> fetchTaxonomy() async => const [
        EmergencyCategory(
          id: 'respiratoires',
          label: 'Urgences respiratoires',
          directCallOnly: false,
          symptoms: [Symptom(id: 'crise-asthme', label: "Crise d'asthme")],
          serviceCodes: ['pulmonology'],
        ),
      ];
}

class _FakeLocation implements LocationService {
  @override
  Future<UserPosition> currentPosition() async =>
      const UserPosition(latitude: 5.35, longitude: -4.0);

  @override
  Future<void> openSettings(LocationFailure failure) async {}
}

class _ConsentAccepted implements ConsentStore {
  @override
  Future<String?> acceptedTermsVersion() async => currentTermsVersion;

  @override
  Future<void> acceptTerms(String version) async {}
}

Widget _app({
  required SessionStoreProvider session,
  AuthRepository? auth,
}) {
  return ProviderScope(
    overrides: [
      sessionStoreProvider.overrideWithValue(session.store),
      authRepositoryProvider.overrideWithValue(auth ?? FakeAuthRepository()),
      consentStoreProvider.overrideWithValue(_ConsentAccepted()),
      orientationRepositoryProvider.overrideWithValue(_FakeRepository()),
      emergencyTaxonomyRepositoryProvider.overrideWithValue(_FakeTaxonomy()),
      locationServiceProvider.overrideWithValue(_FakeLocation()),
    ],
    child: const PatientApp(),
  );
}

/// Petit conteneur pour préparer le store de session (vide ou avec jeton).
class SessionStoreProvider {
  final InMemorySessionStore store = InMemorySessionStore();

  SessionStoreProvider.empty();

  SessionStoreProvider.withToken(String token) {
    store.saveToken(token);
  }
}

void main() {
  testWidgets("sans session, l'écran d'inscription s'affiche", (tester) async {
    await tester.pumpWidget(_app(session: SessionStoreProvider.empty()));
    await tester.pumpAndSettle();

    expect(find.text('Créer votre compte'), findsOneWidget);
    // L'issue d'urgence et les appels restent accessibles.
    expect(find.text('Urgence — continuer sans compte'), findsOneWidget);
    expect(find.textContaining('185'), findsWidgets);
    // On ne doit PAS voir le parcours d'orientation.
    expect(find.text('Quelle est votre urgence ?'), findsNothing);
  });

  testWidgets("« continuer sans compte » ouvre le parcours (issue d'urgence)",
      (tester) async {
    await tester.pumpWidget(_app(session: SessionStoreProvider.empty()));
    await tester.pumpAndSettle();

    final escape = find.text('Urgence — continuer sans compte');
    await tester.scrollUntilVisible(escape, 200,
        scrollable: find.byType(Scrollable).first);
    await tester.tap(escape);
    await tester.pumpAndSettle();

    // Conditions déjà acceptées → on arrive directement au parcours.
    expect(find.text('Quelle est votre urgence ?'), findsOneWidget);
  });

  testWidgets('avec une session valide, le parcours est accessible directement',
      (tester) async {
    await tester.pumpWidget(
        _app(session: SessionStoreProvider.withToken('jeton-valide')));
    await tester.pumpAndSettle();

    expect(find.text('Créer votre compte'), findsNothing);
    expect(find.text('Quelle est votre urgence ?'), findsOneWidget);
  });

  testWidgets("s'inscrire avec succès ouvre le parcours", (tester) async {
    await tester.pumpWidget(_app(session: SessionStoreProvider.empty()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField).first, '+2250102030405');
    await tester.enterText(find.byType(TextField).last, 'MotDePasse2026');
    final submit = find.widgetWithText(FilledButton, 'S\'inscrire');
    await tester.scrollUntilVisible(submit, 200,
        scrollable: find.byType(Scrollable).first);
    await tester.tap(submit);
    await tester.pumpAndSettle();

    expect(find.text('Quelle est votre urgence ?'), findsOneWidget);
  });
}
