import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/model/recommended_center.dart';

/// Fiche d'un centre recommandé : nom, statut, distance/trajet, raison et
/// actions (appeler le centre, démarrer l'itinéraire). Aucune logique métier :
/// les actions sont des callbacks fournis par la View.
class RecommendationCard extends StatelessWidget {
  const RecommendationCard({
    required this.center,
    this.selected = false,
    this.onTap,
    this.onCall,
    this.onNavigate,
    super.key,
  });

  final RecommendedCenter center;
  final bool selected;
  final VoidCallback? onTap;
  final VoidCallback? onCall;
  final VoidCallback? onNavigate;

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
      shape: selected
          ? RoundedRectangleBorder(
              borderRadius: AppRadius.card,
              side: BorderSide(
                  color: Theme.of(context).colorScheme.primary, width: 2),
            )
          : null,
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.card,
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.md),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(child: Text(center.name, style: AppTypography.title)),
                  StatusBadge.fromApi(center.status),
                ],
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(_distanceLabel, style: AppTypography.body),
              const SizedBox(height: AppSpacing.xs),
              Text(center.explanation, style: AppTypography.caption),
              const SizedBox(height: AppSpacing.sm),
              Row(
                children: [
                  // Sans téléphone connu, l'action d'appel est absente
                  // (gestion explicite des données manquantes).
                  if (center.phone != null) ...[
                    OutlinedButton.icon(
                      onPressed: onCall,
                      icon: const Icon(Icons.call),
                      label: const Text('Appeler'),
                    ),
                    const SizedBox(width: AppSpacing.sm),
                  ],
                  FilledButton.icon(
                    onPressed: onNavigate,
                    icon: const Icon(Icons.directions),
                    label: const Text('Itinéraire'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
