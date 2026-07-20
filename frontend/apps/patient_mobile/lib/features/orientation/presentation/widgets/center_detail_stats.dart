import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/model/recommended_center.dart';

/// Statistiques d'un centre (distance, temps de trajet) présentées en grille.
///
/// Le temps de trajet est qualifié honnêtement : « estimé » quand il provient
/// du mode dégradé (distance à vol d'oiseau) plutôt que du fournisseur.
class CenterDetailStats extends StatelessWidget {
  const CenterDetailStats({required this.center, super.key});

  final RecommendedCenter center;

  String get _distanceLabel =>
      '${(center.distanceMeters / 1000.0).toStringAsFixed(1)} km';

  String get _travelLabel {
    final double? seconds = center.travelTimeSeconds;
    if (seconds == null) return 'Indisponible';
    final int minutes = (seconds / 60).round().clamp(1, 999);
    final String prefix = center.travelTimeQuality == 'ESTIMATED' ? '~' : '';
    return '$prefix$minutes min';
  }

  bool get _isEstimated =>
      center.travelTimeSeconds == null ||
      center.travelTimeQuality == 'ESTIMATED';

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: _StatTile(
            icon: Icons.place_outlined,
            value: _distanceLabel,
            label: 'Distance',
          ),
        ),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: _StatTile(
            icon: Icons.directions_car_outlined,
            value: _travelLabel,
            label: _isEstimated ? 'Temps estimé' : 'Temps de trajet',
          ),
        ),
      ],
    );
  }
}

class _StatTile extends StatelessWidget {
  const _StatTile({
    required this.icon,
    required this.value,
    required this.label,
  });

  final IconData icon;
  final String value;
  final String label;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHighest,
        borderRadius: AppRadius.cardLarge,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: scheme.primary),
          const SizedBox(height: AppSpacing.sm),
          Text(value, style: AppTypography.title),
          Text(label, style: AppTypography.caption),
        ],
      ),
    );
  }
}
