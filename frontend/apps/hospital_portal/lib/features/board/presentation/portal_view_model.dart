import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../domain/model/facility_summary.dart';
import '../domain/model/service_line.dart';
import '../domain/portal_auth_exception.dart';
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
    state = state.copyWith(updatingServiceCode: line.serviceCode, clearUpdateError: true);
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
    } on PortalAuthException catch (error) {
      state = state.copyWith(clearUpdating: true);
      switch (error.failure) {
        case PortalAuthFailure.unauthenticated:
          // Jeton absent/invalide/révoqué : rien à réessayer ici, l'agent doit
          // se reconnecter. Même chemin que le bouton de déconnexion (voir
          // logout()) : la porte d'authentification (PortalApp) bascule
          // automatiquement vers l'écran de connexion.
          await logout();
        case PortalAuthFailure.forbidden:
          // Jeton valide : l'agent reste sur le tableau, message ciblé.
          state = state.copyWith(
            updateError: "Cet agent n'est pas autorisé sur cet établissement.",
          );
      }
    } on Exception {
      state = state.copyWith(
        clearUpdating: true,
        updateError: 'La mise à jour a échoué. Réessayez.',
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

  /// Déconnecte l'agent : efface le jeton, fait basculer la porte
  /// d'authentification vers l'écran de connexion, ET repart d'un tableau
  /// vierge (portalViewModelProvider n'est pas `.autoDispose` : sans cette
  /// ré-initialisation explicite, un poste partagé afficherait encore
  /// l'établissement et les lignes de service de l'agent précédent au agent
  /// suivant qui se connecte). `ref.invalidate(portalViewModelProvider)` est
  /// impossible ici : Riverpod interdit explicitement à un provider de
  /// s'invalider lui-même (assertion « A provider cannot depend on itself »,
  /// constatée en test réel) — d'où la réinitialisation manuelle, qui
  /// reproduit `build()`.
  Future<void> logout() async {
    await ref.read(tokenStoreProvider).clear();
    ref.invalidate(sessionTokenProvider);
    state = const PortalState();
    // Volontairement non attendu (même choix que build(), qui ne peut pas
    // await) : logout() n'a pas besoin d'attendre le rechargement pour
    // rendre la main à la porte d'authentification.
    unawaited(Future.microtask(loadFacilities));
  }
}
