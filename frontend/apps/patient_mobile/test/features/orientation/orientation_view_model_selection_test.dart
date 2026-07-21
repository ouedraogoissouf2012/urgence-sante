import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_state.dart';
import 'package:patient_mobile/features/orientation/presentation/orientation_view_model.dart';

import 'support/orientation_fakes.dart';

/// Sélection d'un centre → jeton de recentrage de la carte. Isolé de la suite
/// principale du view-model (responsabilité unique, limite de 300 lignes).
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
}
