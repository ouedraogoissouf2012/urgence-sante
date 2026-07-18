import '../domain/model/facility_summary.dart';
import '../domain/model/service_line.dart';

/// Phase du portail hospitalier.
enum PortalPhase {
  /// Chargement de la liste des établissements (accès démo).
  loadingFacilities,

  /// Sélection de l'établissement par l'agent.
  selectFacility,

  /// Chargement du tableau de disponibilité.
  loadingBoard,

  /// Tableau affiché, modifiable.
  board,

  /// Échec avec message et réessai possible.
  error,
}

/// État immuable du portail (flux unidirectionnel).
class PortalState {
  const PortalState({
    this.phase = PortalPhase.loadingFacilities,
    this.facilities = const [],
    this.selectedFacility,
    this.lines = const [],
    this.updatingServiceCode,
    this.errorMessage,
  });

  final PortalPhase phase;
  final List<FacilitySummary> facilities;
  final FacilitySummary? selectedFacility;
  final List<ServiceLine> lines;

  /// Service en cours de mise à jour (désactive ses contrôles).
  final String? updatingServiceCode;
  final String? errorMessage;

  PortalState copyWith({
    PortalPhase? phase,
    List<FacilitySummary>? facilities,
    FacilitySummary? selectedFacility,
    List<ServiceLine>? lines,
    String? updatingServiceCode,
    String? errorMessage,
    bool clearUpdating = false,
  }) {
    return PortalState(
      phase: phase ?? this.phase,
      facilities: facilities ?? this.facilities,
      selectedFacility: selectedFacility ?? this.selectedFacility,
      lines: lines ?? this.lines,
      updatingServiceCode:
          clearUpdating ? null : (updatingServiceCode ?? this.updatingServiceCode),
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}
