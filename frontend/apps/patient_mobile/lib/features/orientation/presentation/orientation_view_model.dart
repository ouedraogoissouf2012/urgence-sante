import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/location/location_service.dart';
import '../../../di/providers.dart';
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
    // Aucun auto-chargement : la recherche est déclenchée par le choix d'un
    // symptôme (searchForServices), depuis OrientationResultsPage.
    return const OrientationState();
  }

  /// Position de repli (Plateau, centre d'Abidjan) pour le parcours dégradé
  /// sans localisation automatique. Clairement signalée comme approximative.
  static const double _fallbackLatitude = 5.3364;
  static const double _fallbackLongitude = -4.0267;

  /// Recherche multi-services (parcours par symptôme) : le symptôme choisi
  /// couvre [serviceCodes] (sémantique OU). Réutilise localisation et repli.
  Future<void> searchForServices(List<String> serviceCodes) async {
    state = state.copyWith(
      phase: OrientationPhase.searching,
      searchServiceCodes: serviceCodes,
      clearLocationFailure: true,
      clearSelectedCenter: true,
      clearOffline: true,
      approximatePosition: false,
    );
    await _locateAndSearch();
  }

  /// Localise l'utilisateur puis recherche ; bascule en dégradé sur échec.
  Future<void> _locateAndSearch() async {
    try {
      final UserPosition position = await _locationService.currentPosition();
      await _search(position.latitude, position.longitude, approximate: false);
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
      clearErrorMessage: true,
    );
  }

  /// Parcours dégradé : recherche depuis le centre d'Abidjan, sans position
  /// précise (proposé quand la localisation est refusée ou indisponible).
  Future<void> searchWithApproximatePosition() async {
    if (state.searchServiceCodes.isEmpty) {
      return;
    }
    state = state.copyWith(
      phase: OrientationPhase.searching,
      clearLocationFailure: true,
      clearOffline: true,
    );
    try {
      await _search(_fallbackLatitude, _fallbackLongitude, approximate: true);
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

  Future<void> _search(double latitude, double longitude,
      {required bool approximate}) async {
    state = state.copyWith(
      userLatitude: latitude,
      userLongitude: longitude,
      approximatePosition: approximate,
    );
    final results = await _repository.recommend(
      latitude: latitude,
      longitude: longitude,
      serviceCodes: state.searchServiceCodes,
    );
    state = state.copyWith(
      phase: results.isEmpty ? OrientationPhase.empty : OrientationPhase.results,
      results: results,
      clearErrorMessage: true,
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

  /// Réessaie la recherche courante (rien à faire si aucune recherche lancée).
  Future<void> retry() {
    if (state.searchServiceCodes.isEmpty) {
      return Future.value();
    }
    state = state.copyWith(
      phase: OrientationPhase.searching,
      clearLocationFailure: true,
      clearOffline: true,
    );
    return _locateAndSearch();
  }
}
