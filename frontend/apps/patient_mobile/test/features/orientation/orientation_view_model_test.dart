import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_state.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_view_model.dart';

import 'support/orientation_fakes.dart';

void main() {
  const need = MedicalNeed(code: 'maternity', label: 'Maternité');
  const center = RecommendedCenter(
    facilityId: 'id-1',
    name: 'CHU de Cocody',
    latitude: 5.3496,
    longitude: -3.9851,
    phone: '+2250100000001',
    distanceMeters: 2800,
    travelTimeSeconds: 320,
    status: 'AVAILABLE',
    explanation: 'service disponible',
  );

  late FakeOrientationRepository repository;
  late FakeLocationService location;
  late ProviderContainer container;

  setUp(() {
    repository = FakeOrientationRepository();
    location = FakeLocationService();
    container = ProviderContainer(
      overrides: [
        orientationRepositoryProvider.overrideWithValue(repository),
        locationServiceProvider.overrideWithValue(location),
      ],
    );
    addTearDown(container.dispose);
  });

  Future<void> flush() => Future<void>.delayed(Duration.zero);

  OrientationState state() => container.read(orientationViewModelProvider);

  OrientationViewModel viewModel() =>
      container.read(orientationViewModelProvider.notifier);

  test('charge le catalogue au démarrage', () async {
    viewModel();
    await flush();

    expect(state().phase, OrientationPhase.ready);
    expect(state().needs, [need]);
  });

  test('recherche : position puis résultats', () async {
    repository.results = const [center];
    viewModel();
    await flush();

    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.results);
    expect(state().hasPosition, isTrue);
    expect(state().results.single.name, 'CHU de Cocody');
  });

  test('aucun résultat → état vide', () async {
    viewModel();
    await flush();

    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.empty);
  });

  test('localisation refusée → erreur qualifiée avec la cause', () async {
    location.denied = true;
    viewModel();
    await flush();

    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.error);
    expect(state().errorMessage, contains('Autorisation'));
    expect(state().locationFailure, LocationFailure.denied);
  });

  test('dégradé approximatif PUIS panne réseau : pas d\'état contradictoire', () async {
    // Refus de localisation → recherche approximative → mais le réseau tombe.
    location.denied = true;
    repository.failRecommend = true;
    repository.knownCenters = Cached.fromStore(
      const [
        RecommendedCenter(
          facilityId: 'id-1',
          name: 'CHU',
          latitude: 5.3496,
          longitude: -3.9851,
          distanceMeters: 2800,
          status: 'UNKNOWN',
          explanation: 'données locales',
        ),
      ],
      syncedAt: DateTime.utc(2026, 7, 19, 9),
    );
    viewModel();
    await flush();
    await viewModel().searchFor(need);

    await viewModel().searchWithApproximatePosition();

    // Le repli hors ligne neutralise « approximatif » et la position :
    // aucun bandeau contradictoire, aucune carte trompeuse.
    expect(state().phase, OrientationPhase.results);
    expect(state().offlineResults, isTrue);
    expect(state().approximatePosition, isFalse);
    expect(state().hasPosition, isFalse);
  });

  test('parcours dégradé : recherche sans position précise après refus', () async {
    location.denied = true;
    repository.results = const [center];
    viewModel();
    await flush();
    await viewModel().searchFor(need);

    await viewModel().searchWithApproximatePosition();

    expect(state().phase, OrientationPhase.results);
    expect(state().approximatePosition, isTrue);
    expect(state().locationFailure, isNull);
  });

  test('refus permanent → ouverture des réglages adaptés', () async {
    location.denied = true;
    location.failure = LocationFailure.deniedForever;
    viewModel();
    await flush();
    await viewModel().searchFor(need);

    await viewModel().openLocationSettings();

    expect(location.settingsOpened, [LocationFailure.deniedForever]);
  });

  test('panne réseau : le catalogue vient du cache avec sa date', () async {
    repository.failNeeds = true;
    repository.cachedNeeds = Cached.fromStore(
      const [need],
      syncedAt: DateTime.utc(2026, 7, 17, 10),
    );
    viewModel();
    await flush();

    expect(state().phase, OrientationPhase.ready);
    expect(state().needs, [need]);
    expect(state().offlineSyncedAt, DateTime.utc(2026, 7, 17, 10));
  });

  test('panne réseau en recherche : derniers centres connus, non temps réel',
      () async {
    repository.failRecommend = true;
    repository.knownCenters = Cached.fromStore(
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
      syncedAt: DateTime.utc(2026, 7, 17, 9),
    );
    viewModel();
    await flush();

    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.results);
    expect(state().offlineResults, isTrue);
    expect(state().offlineSyncedAt, DateTime.utc(2026, 7, 17, 9));
    expect(state().results.single.status, 'UNKNOWN');
  });

  test('panne réseau en recherche SANS cache → erreur', () async {
    repository.failRecommend = true;
    viewModel();
    await flush();

    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.error);
  });

  test('échec réseau du catalogue → erreur puis retry recharge', () async {
    repository.failNeeds = true;
    viewModel();
    await flush();
    expect(state().phase, OrientationPhase.error);

    repository.failNeeds = false;
    await viewModel().retry();

    expect(state().phase, OrientationPhase.ready);
  });

  test('échec réseau de la recherche → erreur puis retry relance la recherche', () async {
    repository.failRecommend = true;
    viewModel();
    await flush();
    await viewModel().searchFor(need);
    expect(state().phase, OrientationPhase.error);

    repository.failRecommend = false;
    repository.results = const [center];
    await viewModel().retry();

    expect(state().phase, OrientationPhase.results);
  });

  test('une recherche réussie efface le message d\'erreur précédent', () async {
    // Un échec réseau laisse un errorMessage ; la recherche suivante réussit.
    // L'état de succès ne doit pas traîner le message d'erreur (état mensonger).
    repository.failRecommend = true;
    viewModel();
    await flush();
    await viewModel().searchFor(need);
    expect(state().phase, OrientationPhase.error);
    expect(state().errorMessage, isNotNull);

    repository.failRecommend = false;
    repository.results = const [center];
    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.results);
    expect(state().errorMessage, isNull,
        reason: 'un succès ne doit pas conserver l\'erreur de la tentative précédente');
  });

  group('sélection d\'un centre → jeton de recentrage', () {
    test('sélectionner un centre change le centre ET incrémente le jeton', () async {
      viewModel();
      await flush();
      final int before = state().recenterSeq;

      viewModel().selectCenter('id-1');

      expect(state().selectedCenterId, 'id-1');
      expect(state().recenterSeq, before + 1);
    });

    test('re-sélectionner le MÊME centre incrémente encore le jeton', () async {
      // Cœur du correctif : re-taper le centre déjà sélectionné doit produire une
      // nouvelle intention de recentrage (l'utilisateur veut y revenir après avoir
      // fait glisser la carte à la main), même si l'ID ne change pas.
      viewModel();
      await flush();
      viewModel().selectCenter('id-1');
      final int afterFirst = state().recenterSeq;

      viewModel().selectCenter('id-1');

      expect(state().selectedCenterId, 'id-1');
      expect(state().recenterSeq, afterFirst + 1);
    });

    test('une recherche ne modifie PAS le jeton de recentrage', () async {
      // Un changement de résultats ne doit pas déclencher de recentrage
      // intempestif : seule une sélection explicite le fait.
      repository.results = const [center];
      viewModel();
      await flush();
      final int before = state().recenterSeq;

      await viewModel().searchFor(need);

      expect(state().recenterSeq, before);
    });
  });
}
