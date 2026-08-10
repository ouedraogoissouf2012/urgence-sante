import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/calls/emergency_dial.dart';
import '../../../core/location/location_service.dart';
import '../../../di/providers.dart';
import '../../medical_profile/presentation/emergency_card_page.dart';
import 'center_detail_page.dart';
import 'orientation_state.dart';
import 'orientation_view_model.dart';
import 'widgets/emergency_call_bar.dart';
import 'widgets/orientation_results_view.dart';

/// Écran de résultats atteint depuis la taxonomie : déclenche une recherche
/// multi-services pour [serviceCodes] et n'affiche QUE les résultats (le
/// sélecteur plat est masqué). Réutilise le moteur d'orientation existant.
///
/// Le ViewModel d'orientation est ISOLÉ PAR PAGE via un [ProviderScope] dédié :
/// chaque catégorie ouverte possède son propre état. Sans cela, le ViewModel
/// global partagé faisait « fuiter » résultats et carte d'une catégorie sur la
/// suivante (flash de résultats croisés au montage), empilait des recherches
/// concurrentes (la plus lente écrasant l'autre) et relançait le réessai au
/// retour d'arrière-plan sur toutes les pages encore empilées (issue #108).
class OrientationResultsPage extends StatelessWidget {
  const OrientationResultsPage({
    required this.serviceCodes,
    required this.title,
    super.key,
  });

  final List<String> serviceCodes;
  final String title;

  @override
  Widget build(BuildContext context) {
    return ProviderScope(
      // Instance neuve, propre à cette page : l'état ne peut plus être partagé
      // avec une autre catégorie encore empilée dans le navigateur.
      overrides: [
        orientationViewModelProvider.overrideWith(OrientationViewModel.new),
      ],
      child: _OrientationResultsScaffold(
        serviceCodes: serviceCodes,
        title: title,
      ),
    );
  }
}

class _OrientationResultsScaffold extends ConsumerStatefulWidget {
  const _OrientationResultsScaffold({
    required this.serviceCodes,
    required this.title,
  });

  final List<String> serviceCodes;
  final String title;

  @override
  ConsumerState<_OrientationResultsScaffold> createState() =>
      _OrientationResultsScaffoldState();
}

class _OrientationResultsScaffoldState
    extends ConsumerState<_OrientationResultsScaffold>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    Future.microtask(() => ref
        .read(orientationViewModelProvider.notifier)
        .searchForServices(widget.serviceCodes));
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState lifecycleState) {
    if (lifecycleState != AppLifecycleState.resumed) {
      return;
    }
    // Seule la page actuellement visible re-tente : les pages encore empilées
    // sous d'autres écrans ne relancent pas de recherche en arrière-plan (#108).
    if (!(ModalRoute.of(context)?.isCurrent ?? true)) {
      return;
    }
    // Retour des réglages pendant une erreur de localisation : on re-tente.
    final OrientationState state = ref.read(orientationViewModelProvider);
    if (state.phase == OrientationPhase.error && state.locationFailure != null) {
      ref.read(orientationViewModelProvider.notifier).retry();
    }
  }

  @override
  Widget build(BuildContext context) {
    final OrientationState state = ref.watch(orientationViewModelProvider);
    final OrientationViewModel viewModel =
        ref.read(orientationViewModelProvider.notifier);
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          IconButton(
            tooltip: 'Ma fiche d\'urgence',
            icon: const Icon(Icons.medical_information_outlined),
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(builder: (_) => const EmergencyCardPage()),
            ),
          ),
        ],
      ),
      body: _body(context, state, viewModel, ref),
      bottomNavigationBar: const EmergencyCallBar(),
    );
  }

  Widget _body(BuildContext context, OrientationState state,
      OrientationViewModel viewModel, WidgetRef ref) {
    return switch (state.phase) {
      OrientationPhase.loadingNeeds || OrientationPhase.searching =>
        const AsyncStateView.loading(message: 'Recherche des centres adaptés…'),
      OrientationPhase.error => _error(state, viewModel),
      OrientationPhase.ready ||
      OrientationPhase.empty ||
      OrientationPhase.results =>
        OrientationResultsView(
          state: state,
          onSelectCenter: (center) {
            viewModel.selectCenter(center.facilityId);
            Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => CenterDetailPage(center: center),
              ),
            );
          },
          onCall: (center) => dialEmergency(
              context, ref.read(emergencyCallerProvider), center.phone!),
          onNavigate: (center) =>
              ref.read(navigationLauncherProvider).navigateTo(
                    latitude: center.latitude,
                    longitude: center.longitude,
                    label: center.name,
                  ),
        ),
    };
  }

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
                failure == LocationFailure.serviceDisabled) ...[
              FilledButton.icon(
                onPressed: viewModel.openLocationSettings,
                icon: const Icon(Icons.settings),
                label: const Text('Ouvrir les réglages'),
              ),
              const SizedBox(height: AppSpacing.sm),
              OutlinedButton(
                onPressed: viewModel.retry,
                child: const Text('Réessayer'),
              ),
            ] else
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
