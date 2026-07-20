import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/location/location_service.dart';
import '../../../di/providers.dart';
import 'center_detail_page.dart';
import 'orientation_state.dart';
import 'orientation_view_model.dart';
import 'widgets/emergency_call_bar.dart';
import 'widgets/orientation_results_view.dart';

/// Écran principal du parcours patient : besoin → centres recommandés, sur une
/// carte immersive.
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
      body: _body(context, state, viewModel, ref),
      bottomNavigationBar: const EmergencyCallBar(),
    );
  }

  Widget _body(BuildContext context, OrientationState state,
      OrientationViewModel viewModel, WidgetRef ref) {
    return switch (state.phase) {
      OrientationPhase.loadingNeeds =>
        const AsyncStateView.loading(message: 'Chargement des besoins médicaux…'),
      OrientationPhase.searching =>
        const AsyncStateView.loading(message: 'Recherche des centres adaptés…'),
      OrientationPhase.error => _error(state, viewModel),
      OrientationPhase.ready ||
      OrientationPhase.empty ||
      OrientationPhase.results =>
        OrientationResultsView(
          state: state,
          onNeedSelected: viewModel.searchFor,
          // Le tap sélectionne (synchro carte) puis ouvre la fiche détail.
          onSelectCenter: (center) {
            viewModel.selectCenter(center.facilityId);
            Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => CenterDetailPage(center: center),
              ),
            );
          },
          onCall: (center) =>
              ref.read(emergencyCallerProvider).call(center.phone!),
          onNavigate: (center) =>
              ref.read(navigationLauncherProvider).navigateTo(
                    latitude: center.latitude,
                    longitude: center.longitude,
                    label: center.name,
                  ),
        ),
    };
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
            const Icon(Icons.error_outline, size: AppSizing.iconLarge),
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
}
