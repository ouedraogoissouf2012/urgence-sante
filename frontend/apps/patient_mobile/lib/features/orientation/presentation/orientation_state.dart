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
    this.recenterSeq = 0,
    this.offlineSyncedAt,
    this.offlineResults = false,
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

  /// Jeton d'intention de recentrage : incrémenté à CHAQUE sélection de centre,
  /// même si le centre est identique. La carte recentre quand ce jeton change,
  /// pas quand `selectedCenterId` change — sinon re-taper le même centre (pour
  /// y revenir après avoir déplacé la carte à la main) ne recentrerait pas.
  ///
  /// Séparer la *commande* (recentrer) de l'*état* (quel centre est sélectionné)
  /// avec un compteur est le compromis assumé ici. DETTE TRACÉE : si la carte
  /// gagne d'autres commandes distinctes (zoomer, ajuster les bornes, animer),
  /// basculer vers un canal de commandes dédié plutôt que d'empiler des jetons.
  final int recenterSeq;

  /// Date de synchronisation des données affichées quand elles proviennent du
  /// cache local (mode hors ligne) ; `null` si les données sont en direct.
  final DateTime? offlineSyncedAt;

  /// Vrai si les RÉSULTATS affichés sont les derniers centres connus (hors
  /// ligne) : statuts non confirmés, aucun temps réel.
  final bool offlineResults;

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
    int? recenterSeq,
    DateTime? offlineSyncedAt,
    bool? offlineResults,
    bool clearLocationFailure = false,
    bool clearSelectedCenter = false,
    bool clearOffline = false,
    bool clearPosition = false,
  }) {
    return OrientationState(
      phase: phase ?? this.phase,
      needs: needs ?? this.needs,
      selectedNeed: selectedNeed ?? this.selectedNeed,
      results: results ?? this.results,
      userLatitude: clearPosition ? null : (userLatitude ?? this.userLatitude),
      userLongitude: clearPosition ? null : (userLongitude ?? this.userLongitude),
      errorMessage: errorMessage ?? this.errorMessage,
      locationFailure:
          clearLocationFailure ? null : (locationFailure ?? this.locationFailure),
      approximatePosition:
          clearPosition ? false : (approximatePosition ?? this.approximatePosition),
      selectedCenterId:
          clearSelectedCenter ? null : (selectedCenterId ?? this.selectedCenterId),
      recenterSeq: recenterSeq ?? this.recenterSeq,
      offlineSyncedAt:
          clearOffline ? null : (offlineSyncedAt ?? this.offlineSyncedAt),
      offlineResults: clearOffline ? false : (offlineResults ?? this.offlineResults),
    );
  }
}
