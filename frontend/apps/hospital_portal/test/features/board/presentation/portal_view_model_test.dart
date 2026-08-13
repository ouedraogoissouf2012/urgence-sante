import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/di/providers.dart';
import 'package:hospital_portal/features/board/domain/model/facility_summary.dart';
import 'package:hospital_portal/features/board/domain/model/service_line.dart';
import 'package:hospital_portal/features/board/domain/portal_auth_exception.dart';
import 'package:hospital_portal/features/board/domain/repository/portal_repository.dart';
import 'package:hospital_portal/features/board/presentation/portal_state.dart';
import 'package:hospital_portal/features/board/presentation/portal_view_model.dart';

import '../../../support/fake_token_store.dart';

/// Faux repository dont `updateStatus` peut être configuré pour échouer
/// (issue #163 : distinguer 401/403 d'une panne réseau générique).
class _FakeRepository implements PortalRepository {
  Object? updateStatusError;

  @override
  Future<List<FacilitySummary>> loadFacilities() async =>
      const [FacilitySummary(id: 'f-1', name: 'CHU de Cocody')];

  @override
  Future<List<ServiceLine>> loadBoard(String facilityId) async => const [
        ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN'),
      ];

  @override
  Future<ServiceLine> updateStatus({
    required String facilityId,
    required ServiceLine line,
    required String status,
  }) async {
    if (updateStatusError != null) {
      throw updateStatusError!;
    }
    return line.withUpdate(status: status, freshness: 'FRESH', updatedAt: DateTime.utc(2026));
  }

  @override
  Future<List<HistoryEntry>> history(String facilityId, String serviceCode) async => const [];
}

void main() {
  late _FakeRepository repository;
  late FakeTokenStore tokenStore;
  late ProviderContainer container;

  setUp(() async {
    repository = _FakeRepository();
    tokenStore = FakeTokenStore('a-valid-token');
    container = ProviderContainer(overrides: [
      portalRepositoryProvider.overrideWithValue(repository),
      tokenStoreProvider.overrideWithValue(tokenStore),
    ]);
    addTearDown(container.dispose);
    // Amène le ViewModel jusqu'au tableau, comme un agent qui a déjà choisi
    // son établissement.
    await container.read(portalViewModelProvider.notifier).loadFacilities();
    await container
        .read(portalViewModelProvider.notifier)
        .enter(const FacilitySummary(id: 'f-1', name: 'CHU de Cocody'));
  });

  test(
      'un 401 efface le jeton, invalide la session (déconnexion automatique) ET '
      "réinitialise le tableau (portalViewModelProvider n'est pas .autoDispose : sans "
      "cela, l'agent suivant sur un poste partagé verrait l'établissement/les lignes de "
      "l'agent précédent)", () async {
    repository.updateStatusError = const PortalAuthException(PortalAuthFailure.unauthenticated);
    final viewModel = container.read(portalViewModelProvider.notifier);
    expect(container.read(portalViewModelProvider).selectedFacility, isNotNull);

    await viewModel.setStatus(
      const ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN'),
      'LIMITED',
    );

    expect(await tokenStore.readToken(), isNull);
    final state = container.read(portalViewModelProvider);
    // portalViewModelProvider a été invalidé : cette lecture reconstruit un
    // PortalViewModel neuf (état par défaut), pas l'ancienne instance vidée
    // champ par champ.
    expect(state.selectedFacility, isNull);
    expect(state.lines, isEmpty);
    expect(state.updatingServiceCode, isNull);
    expect(state.updateError, isNull);
  });

  test('un 403 affiche un message ciblé SANS effacer le jeton ni quitter le tableau',
      () async {
    repository.updateStatusError = const PortalAuthException(PortalAuthFailure.forbidden);
    final viewModel = container.read(portalViewModelProvider.notifier);

    await viewModel.setStatus(
      const ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN'),
      'LIMITED',
    );

    expect(await tokenStore.readToken(), 'a-valid-token');
    final state = container.read(portalViewModelProvider);
    expect(state.phase, PortalPhase.board);
    expect(state.updateError, "Cet agent n'est pas autorisé sur cet établissement.");
  });

  test('une erreur générique (réseau) affiche un message générique, distinct du 401/403',
      () async {
    repository.updateStatusError = Exception('panne réseau');
    final viewModel = container.read(portalViewModelProvider.notifier);

    await viewModel.setStatus(
      const ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN'),
      'LIMITED',
    );

    expect(await tokenStore.readToken(), 'a-valid-token');
    final state = container.read(portalViewModelProvider);
    expect(state.updateError, 'La mise à jour a échoué. Réessayez.');
  });

  test('un nouvel essai efface le message d\'échec précédent dès le départ', () async {
    repository.updateStatusError = const PortalAuthException(PortalAuthFailure.forbidden);
    final viewModel = container.read(portalViewModelProvider.notifier);
    const line = ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN');
    await viewModel.setStatus(line, 'LIMITED');
    expect(container.read(portalViewModelProvider).updateError, isNotNull);

    repository.updateStatusError = null;
    final future = viewModel.setStatus(line, 'AVAILABLE');
    // Juste après le lancement (avant la résolution du Future), l'échec
    // précédent doit déjà être effacé.
    expect(container.read(portalViewModelProvider).updateError, isNull);
    await future;
  });

  test('logout() efface le jeton ET réinitialise le tableau', () async {
    expect(container.read(portalViewModelProvider).selectedFacility, isNotNull);

    await container.read(portalViewModelProvider.notifier).logout();

    expect(await tokenStore.readToken(), isNull);
    expect(container.read(portalViewModelProvider).selectedFacility, isNull);
  });
}
