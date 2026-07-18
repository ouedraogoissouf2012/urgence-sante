import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/location/location_service.dart';
import '../../../di/providers.dart';
import 'orientation_state.dart';
import 'orientation_view_model.dart';
import 'widgets/emergency_call_bar.dart';
import 'widgets/need_selector.dart';
import 'widgets/position_map.dart';
import 'widgets/recommendation_card.dart';

/// Écran principal du parcours patient : besoin → centres recommandés.
///
/// Aucune logique métier ici : la View observe l'état du ViewModel et lui
/// délègue toutes les actions (flux unidirectionnel).
class OrientationPage extends ConsumerWidget {
  const OrientationPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final OrientationState state = ref.watch(orientationViewModelProvider);
    final OrientationViewModel viewModel =
        ref.read(orientationViewModelProvider.notifier);

    return Scaffold(
      appBar: AppBar(title: const Text('Urgence Santé')),
      body: _body(state, viewModel, ref),
      bottomNavigationBar: const EmergencyCallBar(),
    );
  }

  Widget _body(OrientationState state, OrientationViewModel viewModel, WidgetRef ref) {
    return switch (state.phase) {
      OrientationPhase.loadingNeeds =>
        const AsyncStateView.loading(message: 'Chargement des besoins médicaux…'),
      OrientationPhase.searching =>
        const AsyncStateView.loading(message: 'Recherche des centres adaptés…'),
      OrientationPhase.error => _error(state, viewModel),
      OrientationPhase.ready ||
      OrientationPhase.empty ||
      OrientationPhase.results =>
        _content(state, viewModel, ref),
    };
  }

  /// Libellé lisible de la date de synchronisation des données locales.
  static String _syncLabel(DateTime syncedAt) {
    final local = syncedAt.toLocal();
    String two(int value) => value.toString().padLeft(2, '0');
    return 'synchronisées le ${two(local.day)}/${two(local.month)} '
        'à ${two(local.hour)}:${two(local.minute)}';
  }

  /// Erreur avec actions adaptées : réessai, réglages si nécessaire, et
  /// parcours dégradé sans localisation précise quand la position est en cause.
  Widget _error(OrientationState state, OrientationViewModel viewModel) {
    final LocationFailure? failure = state.locationFailure;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48),
            const SizedBox(height: AppSpacing.md),
            Text(
              state.errorMessage ?? 'Une erreur est survenue.',
              style: AppTypography.body,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.lg),
            if (failure == LocationFailure.deniedForever ||
                failure == LocationFailure.serviceDisabled)
              FilledButton.icon(
                onPressed: viewModel.openLocationSettings,
                icon: const Icon(Icons.settings),
                label: const Text('Ouvrir les réglages'),
              )
            else
              FilledButton(
                onPressed: viewModel.retry,
                child: const Text('Réessayer'),
              ),
            if (failure != null) ...[
              const SizedBox(height: AppSpacing.sm),
              TextButton(
                onPressed: viewModel.searchWithApproximatePosition,
                child: const Text('Continuer sans position précise'),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _content(OrientationState state, OrientationViewModel viewModel, WidgetRef ref) {
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        const Text('De quel soin avez-vous besoin ?', style: AppTypography.title),
        const SizedBox(height: AppSpacing.sm),
        NeedSelector(
          needs: state.needs,
          selected: state.selectedNeed,
          onSelected: viewModel.searchFor,
        ),
        const SizedBox(height: AppSpacing.md),
        if (state.offlineSyncedAt != null) ...[
          Card(
            color: AppColors.statusSaturated.withValues(alpha: 0.12),
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.sm),
              child: Text(
                state.offlineResults
                    ? 'Hors ligne : derniers centres connus '
                        '(${_syncLabel(state.offlineSyncedAt!)}). '
                        'Disponibilités non confirmées.'
                    : 'Hors ligne : données locales (${_syncLabel(state.offlineSyncedAt!)}).',
                style: AppTypography.caption,
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
        if (state.approximatePosition) ...[
          Card(
            color: AppColors.statusLimited.withValues(alpha: 0.12),
            child: const Padding(
              padding: EdgeInsets.all(AppSpacing.sm),
              child: Text(
                'Position approximative (centre d\'Abidjan) : les distances '
                'sont indicatives.',
                style: AppTypography.caption,
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
        if (state.hasPosition) ...[
          SizedBox(
            height: 220,
            child: ClipRRect(
              borderRadius: AppRadius.card,
              child: PositionMap(
                latitude: state.userLatitude!,
                longitude: state.userLongitude!,
                centers: state.results,
                selectedCenterId: state.selectedCenterId,
                onCenterTap: (center) => viewModel.selectCenter(center.facilityId),
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
        ],
        if (state.phase == OrientationPhase.empty)
          const AsyncStateView.empty(
            message: 'Aucun centre trouvé pour ce besoin autour de vous.',
          ),
        if (state.phase == OrientationPhase.results) ...[
          const Text('Centres recommandés', style: AppTypography.title),
          const SizedBox(height: AppSpacing.sm),
          for (final center in state.results)
            RecommendationCard(
              center: center,
              selected: center.facilityId == state.selectedCenterId,
              onTap: () => viewModel.selectCenter(center.facilityId),
              onCall: center.phone == null
                  ? null
                  : () => ref.read(emergencyCallerProvider).call(center.phone!),
              onNavigate: () => ref.read(navigationLauncherProvider).navigateTo(
                    latitude: center.latitude,
                    longitude: center.longitude,
                    label: center.name,
                  ),
            ),
        ],
      ],
    );
  }
}
