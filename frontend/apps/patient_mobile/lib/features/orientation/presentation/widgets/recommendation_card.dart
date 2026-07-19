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

  /// Largeur maximale du badge de statut avant repli du texte.
  static const double _badgeMaxWidth = 140.0;

  final RecommendedCenter center;
  final bool selected;
  final VoidCallback? onTap;
  final VoidCallback? onCall;
  final VoidCallback? onNavigate;

  String get _distanceLabel {
    final double km = center.distanceMeters / 1000.0;
    final double? seconds = center.travelTimeSeconds;
    if (seconds == null) {
      // Aucun temps de trajet du fournisseur. On signale le mode dégradé
      // (qualité « ESTIMATED ») pour ne pas laisser croire à un simple manque.
      final String suffix =
          center.travelTimeQuality == 'ESTIMATED' ? ' · temps estimé' : '';
      return '${km.toStringAsFixed(1)} km$suffix';
    }
    final int minutes = (seconds / 60).round().clamp(1, 999);
    return '${km.toStringAsFixed(1)} km · ~$minutes min';
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: AppRadius.cardLarge,
        side: selected
            ? BorderSide(
                color: Theme.of(context).colorScheme.primary, width: 2)
            : BorderSide.none,
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.cardLarge,
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.md),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(child: Text(center.name, style: AppTypography.title)),
                  const SizedBox(width: AppSpacing.sm),
                  // Largeur bornée : à très grande police le texte du badge se
                  // replie au lieu de déborder (issue #47).
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: _badgeMaxWidth),
                    child: StatusBadge.fromApi(center.status),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(_distanceLabel, style: AppTypography.body),
              const SizedBox(height: AppSpacing.xs),
              Text(center.explanation, style: AppTypography.caption),
              const SizedBox(height: AppSpacing.sm),
              // Wrap (et non Row) : les actions passent à la ligne sur petit
              // écran ou grande police au lieu de déborder (issue #47).
              Wrap(
                spacing: AppSpacing.sm,
                runSpacing: AppSpacing.sm,
                children: [
                  // Sans téléphone connu, l'action d'appel est absente
                  // (gestion explicite des données manquantes).
                  if (center.phone != null)
                    Semantics(
                      button: true,
                      label: 'Appeler ${center.name}',
                      child: OutlinedButton.icon(
                        onPressed: onCall,
                        icon: const Icon(Icons.call),
                        label: const Text('Appeler'),
                      ),
                    ),
                  Semantics(
                    button: true,
                    label: 'Itinéraire vers ${center.name}',
                    child: FilledButton.icon(
                      onPressed: onNavigate,
                      icon: const Icon(Icons.directions),
                      label: const Text('Itinéraire'),
                    ),
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
