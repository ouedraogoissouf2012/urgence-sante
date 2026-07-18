import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/model/recommended_center.dart';

/// Carte d'un centre recommandé : nom, statut, distance/trajet et raison.
class RecommendationCard extends StatelessWidget {
  const RecommendationCard({required this.center, super.key});

  final RecommendedCenter center;

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
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(center.name, style: AppTypography.title),
                ),
                StatusBadge.fromApi(center.status),
              ],
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(_distanceLabel, style: AppTypography.body),
            const SizedBox(height: AppSpacing.xs),
            Text(center.explanation, style: AppTypography.caption),
          ],
        ),
      ),
    );
  }
}
