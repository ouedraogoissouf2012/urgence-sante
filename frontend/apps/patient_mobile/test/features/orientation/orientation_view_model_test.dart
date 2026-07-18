import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_state.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_view_model.dart';

/// Faux repository configurable, substituable au vrai adaptateur API.
class FakeOrientationRepository implements OrientationRepository {
  List<MedicalNeed> needs = const [MedicalNeed(code: 'maternity', label: 'Maternité')];
  List<RecommendedCenter> results = const [];
  bool failNeeds = false;
  bool failRecommend = false;
  Cached<List<MedicalNeed>>? cachedNeeds;
  Cached<List<RecommendedCenter>>? knownCenters;

  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async {
    if (failNeeds) {
      final fallback = cachedNeeds;
      if (fallback != null) return fallback;
      throw Exception('réseau');
    }
    return Cached.live(needs);
  }

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  }) async {
    if (failRecommend) throw Exception('réseau');
    return results;
  }

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters() async => knownCenters;
}

/// Fausse localisation, substituable à Geolocator.
class FakeLocationService implements LocationService {
  bool denied = false;
  LocationFailure failure = LocationFailure.denied;
  final List<LocationFailure> settingsOpened = [];

  @override
  Future<UserPosition> currentPosition() async {
    if (denied) {
      throw LocationUnavailableException('Autorisation refusée.', failure);
    }
    return const UserPosition(latitude: 5.35, longitude: -4.0);
  }

  @override
  Future<void> openSettings(LocationFailure failure) async =>
      settingsOpened.add(failure);
}

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
}
