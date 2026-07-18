import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

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
      body: _body(state, viewModel),
      bottomNavigationBar: const EmergencyCallBar(),
    );
  }

  Widget _body(OrientationState state, OrientationViewModel viewModel) {
    return switch (state.phase) {
      OrientationPhase.loadingNeeds =>
        const AsyncStateView.loading(message: 'Chargement des besoins médicaux…'),
      OrientationPhase.searching =>
        const AsyncStateView.loading(message: 'Recherche des centres adaptés…'),
      OrientationPhase.error => AsyncStateView.error(
          message: state.errorMessage ?? 'Une erreur est survenue.',
          onRetry: viewModel.retry,
        ),
      OrientationPhase.ready ||
      OrientationPhase.empty ||
      OrientationPhase.results =>
        _content(state, viewModel),
    };
  }

  Widget _content(OrientationState state, OrientationViewModel viewModel) {
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
        if (state.hasPosition) ...[
          SizedBox(
            height: 220,
            child: ClipRRect(
              borderRadius: AppRadius.card,
              child: PositionMap(
                latitude: state.userLatitude!,
                longitude: state.userLongitude!,
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
          for (final center in state.results) RecommendationCard(center: center),
        ],
      ],
    );
  }
}
