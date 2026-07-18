import '../../../core/location/location_service.dart';
import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';

/// Phase du parcours d'orientation.
enum OrientationPhase {
  /// Chargement initial du catalogue des besoins.
  loadingNeeds,

  /// Catalogue affiché, en attente d'une recherche.
  ready,

  /// Localisation puis recherche en cours.
  searching,

  /// Résultats disponibles.
  results,

  /// Recherche aboutie mais aucun centre trouvé.
  empty,

  /// Échec (réseau, localisation…) avec message et réessai possible.
  error,
}

/// État immuable du parcours d'orientation (flux unidirectionnel).
class OrientationState {
  const OrientationState({
    this.phase = OrientationPhase.loadingNeeds,
    this.needs = const [],
    this.selectedNeed,
    this.results = const [],
    this.userLatitude,
    this.userLongitude,
    this.errorMessage,
    this.locationFailure,
    this.approximatePosition = false,
    this.selectedCenterId,
  });

  final OrientationPhase phase;
  final List<MedicalNeed> needs;
  final MedicalNeed? selectedNeed;
  final List<RecommendedCenter> results;
  final double? userLatitude;
  final double? userLongitude;
  final String? errorMessage;

  /// Cause de l'échec de localisation (pour proposer l'action adaptée).
  final LocationFailure? locationFailure;

  /// Vrai si la recherche a utilisé une position approximative (mode dégradé
  /// sans localisation automatique) — affiché clairement à l'utilisateur.
  final bool approximatePosition;

  /// Centre sélectionné (synchronisation carte ↔ liste), ou `null`.
  final String? selectedCenterId;

  bool get hasPosition => userLatitude != null && userLongitude != null;

  OrientationState copyWith({
    OrientationPhase? phase,
    List<MedicalNeed>? needs,
    MedicalNeed? selectedNeed,
    List<RecommendedCenter>? results,
    double? userLatitude,
    double? userLongitude,
    String? errorMessage,
    LocationFailure? locationFailure,
    bool? approximatePosition,
    String? selectedCenterId,
    bool clearLocationFailure = false,
    bool clearSelectedCenter = false,
  }) {
    return OrientationState(
      phase: phase ?? this.phase,
      needs: needs ?? this.needs,
      selectedNeed: selectedNeed ?? this.selectedNeed,
      results: results ?? this.results,
      userLatitude: userLatitude ?? this.userLatitude,
      userLongitude: userLongitude ?? this.userLongitude,
      errorMessage: errorMessage ?? this.errorMessage,
      locationFailure:
          clearLocationFailure ? null : (locationFailure ?? this.locationFailure),
      approximatePosition: approximatePosition ?? this.approximatePosition,
      selectedCenterId:
          clearSelectedCenter ? null : (selectedCenterId ?? this.selectedCenterId),
    );
  }
}
