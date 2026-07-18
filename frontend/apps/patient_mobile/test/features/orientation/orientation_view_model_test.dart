import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/di/providers.dart';
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

  @override
  Future<List<MedicalNeed>> loadNeeds() async {
    if (failNeeds) throw Exception('réseau');
    return needs;
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
}

/// Fausse localisation, substituable à Geolocator.
class FakeLocationService implements LocationService {
  bool denied = false;

  @override
  Future<UserPosition> currentPosition() async {
    if (denied) {
      throw const LocationUnavailableException('Autorisation refusée.');
    }
    return const UserPosition(latitude: 5.35, longitude: -4.0);
  }
}

void main() {
  const need = MedicalNeed(code: 'maternity', label: 'Maternité');
  const center = RecommendedCenter(
    facilityId: 'id-1',
    name: 'CHU de Cocody',
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

  test('localisation refusée → erreur avec message dédié', () async {
    location.denied = true;
    viewModel();
    await flush();

    await viewModel().searchFor(need);

    expect(state().phase, OrientationPhase.error);
    expect(state().errorMessage, contains('Autorisation'));
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
