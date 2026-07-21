import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/model/medical_need.dart';
import '../../domain/model/recommended_center.dart';
import '../orientation_state.dart';
import 'context_banner.dart';
import 'need_selector.dart';
import 'position_map.dart';
import 'recommendation_card.dart';
import 'recommendation_card_compact.dart';

/// Vue immersive des résultats : la carte occupe tout l'écran et le besoin
/// médical puis les centres remontent dans une feuille glissante. Le premier
/// centre est mis en avant (carte riche), les suivants sont compacts. Aucune
/// logique métier : toutes les actions sont des callbacks fournis par la View.
class OrientationResultsView extends StatelessWidget {
  const OrientationResultsView({
    required this.state,
    required this.onNeedSelected,
    required this.onSelectCenter,
    required this.onCall,
    required this.onNavigate,
    super.key,
  });

  final OrientationState state;
  final ValueChanged<MedicalNeed> onNeedSelected;
  final ValueChanged<RecommendedCenter> onSelectCenter;
  final ValueChanged<RecommendedCenter> onCall;
  final ValueChanged<RecommendedCenter> onNavigate;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Stack(
      children: [
        if (state.hasPosition)
          Positioned.fill(
            child: PositionMap(
              latitude: state.userLatitude!,
              longitude: state.userLongitude!,
              centers: state.results,
              selectedCenterId: state.selectedCenterId,
              recenterSeq: state.recenterSeq,
              onCenterTap: onSelectCenter,
            ),
          )
        else
          Positioned.fill(
            child: ColoredBox(color: scheme.surfaceContainerHighest),
          ),
        DraggableScrollableSheet(
          initialChildSize: state.hasPosition ? 0.5 : 0.92,
          minChildSize: 0.28,
          maxChildSize: 0.92,
          builder: (context, controller) => _Sheet(
            controller: controller,
            state: state,
            onNeedSelected: onNeedSelected,
            onSelectCenter: onSelectCenter,
            onCall: onCall,
            onNavigate: onNavigate,
          ),
        ),
      ],
    );
  }
}

class _Sheet extends StatelessWidget {
  const _Sheet({
    required this.controller,
    required this.state,
    required this.onNeedSelected,
    required this.onSelectCenter,
    required this.onCall,
    required this.onNavigate,
  });

  final ScrollController controller;
  final OrientationState state;
  final ValueChanged<MedicalNeed> onNeedSelected;
  final ValueChanged<RecommendedCenter> onSelectCenter;
  final ValueChanged<RecommendedCenter> onCall;
  final ValueChanged<RecommendedCenter> onNavigate;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    final List<RecommendedCenter> results = state.results;
    final bool hasResults = state.phase == OrientationPhase.results;
    return Material(
      color: scheme.surface,
      shape: const RoundedRectangleBorder(borderRadius: AppRadius.sheetTop),
      clipBehavior: Clip.antiAlias,
      child: ListView(
        controller: controller,
        padding: const EdgeInsets.fromLTRB(
            AppSpacing.md, AppSpacing.sm, AppSpacing.md, AppSpacing.lg),
        children: [
          const _Grip(),
          const Text('De quel soin avez-vous besoin ?',
              style: AppTypography.title),
          const SizedBox(height: AppSpacing.sm),
          NeedSelector(
            needs: state.needs,
            selected: state.selectedNeed,
            onSelected: onNeedSelected,
          ),
          const SizedBox(height: AppSpacing.md),
          if (state.offlineSyncedAt != null) ...[
            ContextBanner(
              tone: ContextBannerTone.offline,
              message: state.offlineResults
                  ? 'Hors ligne : derniers centres connus. '
                      'Disponibilités non confirmées.'
                  : 'Hors ligne : données locales.',
            ),
            const SizedBox(height: AppSpacing.sm),
          ],
          if (state.approximatePosition) ...[
            const ContextBanner(
              tone: ContextBannerTone.approximate,
              message: 'Position approximative (centre d\'Abidjan) : '
                  'les distances sont indicatives.',
            ),
            const SizedBox(height: AppSpacing.sm),
          ],
          if (state.phase == OrientationPhase.empty)
            const Text(
              'Aucun centre trouvé pour ce besoin autour de vous.',
              style: AppTypography.body,
            ),
          if (hasResults) ...[
            Text(
              '${results.length} centre${results.length > 1 ? 's' : ''} '
              'adapté${results.length > 1 ? 's' : ''}',
              style: AppTypography.title,
            ),
            const SizedBox(height: AppSpacing.sm),
            for (int i = 0; i < results.length; i++) ...[
              if (i == 0)
                RecommendationCard(
                  center: results[i],
                  selected: results[i].facilityId == state.selectedCenterId,
                  onTap: () => onSelectCenter(results[i]),
                  onCall: results[i].phone == null
                      ? null
                      : () => onCall(results[i]),
                  onNavigate: () => onNavigate(results[i]),
                )
              else
                RecommendationCardCompact(
                  center: results[i],
                  rank: i + 1,
                  selected: results[i].facilityId == state.selectedCenterId,
                  onTap: () => onSelectCenter(results[i]),
                ),
              const SizedBox(height: AppSpacing.sm),
            ],
          ],
        ],
      ),
    );
  }
}

/// Poignée de préhension de la feuille glissante.
class _Grip extends StatelessWidget {
  const _Grip();

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Center(
      child: Container(
        width: AppSpacing.xl,
        height: AppSpacing.xs,
        margin: const EdgeInsets.only(bottom: AppSpacing.sm),
        decoration: BoxDecoration(
          color: scheme.outlineVariant,
          borderRadius: AppRadius.card,
        ),
      ),
    );
  }
}
