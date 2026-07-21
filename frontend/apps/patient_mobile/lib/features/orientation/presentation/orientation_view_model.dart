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

  /// Charge le catalogue des besoins médicaux (cache local en panne réseau,
  /// avec sa date de synchronisation affichée).
  Future<void> loadNeeds() async {
    state = state.copyWith(phase: OrientationPhase.loadingNeeds, clearOffline: true);
    try {
      final cached = await _repository.loadNeeds();
      state = state.copyWith(
        phase: OrientationPhase.ready,
        needs: cached.value,
        offlineSyncedAt: cached.fromCache ? cached.syncedAt : null,
      );
    } on Exception {
      state = state.copyWith(
        phase: OrientationPhase.error,
        errorMessage:
            'Impossible de charger les besoins médicaux. Vérifiez votre connexion.',
      );
    }
  }

  /// Position de repli (Plateau, centre d'Abidjan) pour le parcours dégradé
  /// sans localisation automatique. Clairement signalée comme approximative.
  static const double _fallbackLatitude = 5.3364;
  static const double _fallbackLongitude = -4.0267;

  /// Sélectionne un besoin puis lance localisation + recherche.
  Future<void> searchFor(MedicalNeed need) async {
    state = state.copyWith(
      phase: OrientationPhase.searching,
      selectedNeed: need,
      clearLocationFailure: true,
      clearSelectedCenter: true,
      clearOffline: true,
      approximatePosition: false,
    );
    try {
      final UserPosition position = await _locationService.currentPosition();
      await _search(need, position.latitude, position.longitude, approximate: false);
    } on LocationUnavailableException catch (exception) {
      state = state.copyWith(
        phase: OrientationPhase.error,
        errorMessage: exception.message,
        locationFailure: exception.failure,
      );
    } on Exception {
      await _fallbackToLastKnownCenters();
    }
  }

  /// En panne réseau, sert les DERNIERS CENTRES CONNUS (annuaire minimal
  /// hors ligne) : statuts non confirmés, date de synchronisation affichée.
  Future<void> _fallbackToLastKnownCenters() async {
    final cached = await _repository.lastKnownCenters();
    if (cached == null) {
      state = state.copyWith(
        phase: OrientationPhase.error,
        errorMessage:
            'La recherche a échoué. Vérifiez votre connexion puis réessayez.',
      );
      return;
    }
    // Hors ligne : on ne dispose pas d'une position fiable du patient. On
    // efface toute position (y compris le repli « approximatif ») pour ne pas
    // afficher une carte trompeuse ni un bandeau « position approximative »
    // contradictoire avec des distances issues du cache.
    state = state.copyWith(
      phase: OrientationPhase.results,
      results: cached.value,
      offlineResults: true,
      offlineSyncedAt: cached.syncedAt,
      clearPosition: true,
    );
  }

  /// Parcours dégradé : recherche depuis le centre d'Abidjan, sans position
  /// précise (proposé quand la localisation est refusée ou indisponible).
  Future<void> searchWithApproximatePosition() async {
    final MedicalNeed? need = state.selectedNeed;
    if (need == null) {
      return;
    }
    state = state.copyWith(
      phase: OrientationPhase.searching,
      clearLocationFailure: true,
      clearOffline: true,
    );
    try {
      await _search(need, _fallbackLatitude, _fallbackLongitude, approximate: true);
    } on Exception {
      await _fallbackToLastKnownCenters();
    }
  }

  /// Ouvre les réglages adaptés à la cause d'échec de localisation.
  Future<void> openLocationSettings() {
    final LocationFailure? failure = state.locationFailure;
    return failure == null
        ? Future.value()
        : _locationService.openSettings(failure);
  }

  Future<void> _search(
      MedicalNeed need, double latitude, double longitude,
      {required bool approximate}) async {
    state = state.copyWith(
      userLatitude: latitude,
      userLongitude: longitude,
      approximatePosition: approximate,
    );
    final results = await _repository.recommend(
      latitude: latitude,
      longitude: longitude,
      serviceCode: need.code,
    );
    state = state.copyWith(
      phase: results.isEmpty ? OrientationPhase.empty : OrientationPhase.results,
      results: results,
    );
  }

  /// Sélectionne un centre (synchronisation carte ↔ liste) et demande le
  /// recentrage de la carte dessus. Le jeton est incrémenté à CHAQUE appel,
  /// même pour le centre déjà sélectionné : re-taper doit ramener la carte sur
  /// le centre (l'utilisateur a pu la déplacer à la main entre-temps).
  void selectCenter(String facilityId) {
    state = state.copyWith(
      selectedCenterId: facilityId,
      recenterSeq: state.recenterSeq + 1,
    );
  }

  /// Réessaie l'action pertinente selon l'état courant.
  Future<void> retry() {
    final MedicalNeed? need = state.selectedNeed;
    return need == null ? loadNeeds() : searchFor(need);
  }
}
