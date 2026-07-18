import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../domain/model/facility_summary.dart';
import '../domain/model/service_line.dart';
import '../domain/repository/portal_repository.dart';
import 'portal_state.dart';

/// ViewModel du portail hospitalier.
final portalViewModelProvider = NotifierProvider<PortalViewModel, PortalState>(
  PortalViewModel.new,
);

/// ViewModel du portail (MVVM, flux unidirectionnel). Testable en pur Dart
/// via la surcharge du repository.
class PortalViewModel extends Notifier<PortalState> {
  PortalRepository get _repository => ref.read(portalRepositoryProvider);

  @override
  PortalState build() {
    Future.microtask(loadFacilities);
    return const PortalState();
  }

  /// Charge les établissements sélectionnables (accès démo).
  Future<void> loadFacilities() async {
    state = state.copyWith(phase: PortalPhase.loadingFacilities);
    try {
      final facilities = await _repository.loadFacilities();
      state = state.copyWith(phase: PortalPhase.selectFacility, facilities: facilities);
    } on Exception {
      state = state.copyWith(
        phase: PortalPhase.error,
        errorMessage: 'Impossible de charger les établissements.',
      );
    }
  }

  /// L'agent accède au tableau de son établissement.
  Future<void> enter(FacilitySummary facility) async {
    state = state.copyWith(phase: PortalPhase.loadingBoard, selectedFacility: facility);
    try {
      final lines = await _repository.loadBoard(facility.id);
      state = state.copyWith(phase: PortalPhase.board, lines: lines);
    } on Exception {
      state = state.copyWith(
        phase: PortalPhase.error,
        errorMessage: 'Impossible de charger le tableau de disponibilité.',
      );
    }
  }

  /// Met à jour le statut d'un service (horodaté côté serveur).
  Future<void> setStatus(ServiceLine line, String status) async {
    final facility = state.selectedFacility;
    if (facility == null || state.updatingServiceCode != null) {
      return;
    }
    state = state.copyWith(updatingServiceCode: line.serviceCode);
    try {
      final updated = await _repository.updateStatus(
        facilityId: facility.id,
        line: line,
        status: status,
      );
      state = state.copyWith(
        lines: [
          for (final current in state.lines)
            current.serviceCode == updated.serviceCode ? updated : current,
        ],
        clearUpdating: true,
      );
    } on Exception {
      state = state.copyWith(
        clearUpdating: true,
        errorMessage: 'La mise à jour a échoué. Réessayez.',
      );
    }
  }

  /// Historique d'un service (affiché par la View).
  Future<List<HistoryEntry>> history(ServiceLine line) {
    final facility = state.selectedFacility;
    if (facility == null) {
      return Future.value(const []);
    }
    return _repository.history(facility.id, line.serviceCode);
  }

  /// Réessaie l'action pertinente selon l'état courant.
  Future<void> retry() {
    final facility = state.selectedFacility;
    return facility == null ? loadFacilities() : enter(facility);
  }
}
