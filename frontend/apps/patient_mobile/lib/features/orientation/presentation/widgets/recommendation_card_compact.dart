import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/model/recommended_center.dart';

/// Version compacte d'un centre recommandé (centres secondaires de la liste) :
/// rang, nom, distance et badge de statut sur une ligne. Un appui ouvre le
/// centre (sélection carte ↔ liste). Aucune logique métier : [onTap] est fourni
/// par la View.
class RecommendationCardCompact extends StatelessWidget {
  const RecommendationCardCompact({
    required this.center,
    required this.rank,
    this.selected = false,
    this.onTap,
    super.key,
  });

  /// Largeur maximale du badge avant repli (grande police, issue #47).
  static const double _badgeMaxWidth = 128.0;

  final RecommendedCenter center;

  /// Rang affiché (2, 3, …) — l'ordre vient de la liste des résultats.
  final int rank;
  final bool selected;
  final VoidCallback? onTap;

  String get _distanceLabel {
    final double km = center.distanceMeters / 1000.0;
    final double? seconds = center.travelTimeSeconds;
    if (seconds == null) {
      return '${km.toStringAsFixed(1)} km';
    }
    final int minutes = (seconds / 60).round().clamp(1, 999);
    return '${km.toStringAsFixed(1)} km · ~$minutes min';
  }

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Card(
      shape: selected
          ? RoundedRectangleBorder(
              borderRadius: AppRadius.card,
              side: BorderSide(color: scheme.primary, width: 2),
            )
          : null,
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.card,
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.sm),
          child: Row(
            children: [
              _RankBadge(rank: rank),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      center.name,
                      style: AppTypography.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(_distanceLabel, style: AppTypography.caption),
                  ],
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: _badgeMaxWidth),
                child: StatusBadge.fromApi(center.status),
              ),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
      ),
    );
  }
}

/// Pastille de rang du centre (numéro d'ordre dans la liste).
class _RankBadge extends StatelessWidget {
  const _RankBadge({required this.rank});

  final int rank;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Container(
      width: AppSpacing.lg,
      height: AppSpacing.lg,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHighest,
        borderRadius: AppRadius.card,
      ),
      child: Text('$rank', style: AppTypography.caption),
    );
  }
}
