import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/location/location_service.dart';
import '../../../di/providers.dart';
import '../domain/model/medical_need.dart';
import '../domain/repository/orientation_repository.dart';
import 'orientation_state.dart';

/// ViewModel du parcours d'orientation.
final orientationViewModelProvider =
    NotifierProvider<OrientationViewModel, OrientationState>(
  OrientationViewModel.new,
);

/// ViewModel du parcours d'orientation (MVVM, flux unidirectionnel).
///
/// Les dépendances sont résolues via les providers (surchargées par des faux
/// en test) ; aucune dépendance widget : testable en pur Dart.
class OrientationViewModel extends Notifier<OrientationState> {
  OrientationRepository get _repository => ref.read(orientationRepositoryProvider);

  LocationService get _locationService => ref.read(locationServiceProvider);

  @override
  OrientationState build() {
    Future.microtask(loadNeeds);
    return const OrientationState();
  }

  /// Charge le catalogue des besoins médicaux.
  Future<void> loadNeeds() async {
    state = state.copyWith(phase: OrientationPhase.loadingNeeds);
    try {
      final List<MedicalNeed> needs = await _repository.loadNeeds();
      state = state.copyWith(phase: OrientationPhase.ready, needs: needs);
    } on Exception {
      state = state.copyWith(
        phase: OrientationPhase.error,
        errorMessage:
            'Impossible de charger les besoins médicaux. Vérifiez votre connexion.',
      );
    }
  }

  /// Sélectionne un besoin puis lance localisation + recherche.
  Future<void> searchFor(MedicalNeed need) async {
    state = state.copyWith(phase: OrientationPhase.searching, selectedNeed: need);
    try {
      final UserPosition position = await _locationService.currentPosition();
      state = state.copyWith(
        userLatitude: position.latitude,
        userLongitude: position.longitude,
      );
      final results = await _repository.recommend(
        latitude: position.latitude,
        longitude: position.longitude,
        serviceCode: need.code,
      );
      state = state.copyWith(
        phase: results.isEmpty ? OrientationPhase.empty : OrientationPhase.results,
        results: results,
      );
    } on LocationUnavailableException catch (exception) {
      state = state.copyWith(
        phase: OrientationPhase.error,
        errorMessage: exception.message,
      );
    } on Exception {
      state = state.copyWith(
        phase: OrientationPhase.error,
        errorMessage:
            'La recherche a échoué. Vérifiez votre connexion puis réessayez.',
      );
    }
  }

  /// Réessaie l'action pertinente selon l'état courant.
  Future<void> retry() {
    final MedicalNeed? need = state.selectedNeed;
    return need == null ? loadNeeds() : searchFor(need);
  }
}
