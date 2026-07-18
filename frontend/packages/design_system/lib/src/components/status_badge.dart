import 'package:flutter/material.dart';

import '../tokens/app_colors.dart';
import '../tokens/app_radius.dart';
import '../tokens/app_spacing.dart';
import '../tokens/app_typography.dart';

/// Statut de disponibilité affichable (aligné sur le contrat d'API).
enum StatusBadgeKind { available, limited, saturated, closed, unknown }

/// Badge de statut de disponibilité.
///
/// L'information est portée par le **texte** et la couleur (jamais la couleur
/// seule — accessibilité daltonisme et lecteurs d'écran).
class StatusBadge extends StatelessWidget {
  const StatusBadge({required this.kind, super.key});

  /// Construit le badge depuis la valeur du contrat d'API (ex. « AVAILABLE »).
  /// Une valeur inconnue est affichée comme non confirmée.
  factory StatusBadge.fromApi(String status, {Key? key}) {
    final StatusBadgeKind kind = switch (status.toUpperCase()) {
      'AVAILABLE' => StatusBadgeKind.available,
      'LIMITED' => StatusBadgeKind.limited,
      'SATURATED' => StatusBadgeKind.saturated,
      'CLOSED' => StatusBadgeKind.closed,
      _ => StatusBadgeKind.unknown,
    };
    return StatusBadge(kind: kind, key: key);
  }

  final StatusBadgeKind kind;

  static const Map<StatusBadgeKind, (String, Color)> _presentation = {
    StatusBadgeKind.available: ('Disponible', AppColors.statusAvailable),
    StatusBadgeKind.limited: ('Limité', AppColors.statusLimited),
    StatusBadgeKind.saturated: ('Saturé', AppColors.statusSaturated),
    StatusBadgeKind.closed: ('Fermé', AppColors.statusClosed),
    StatusBadgeKind.unknown: ('Non confirmé', AppColors.statusUnknown),
  };

  @override
  Widget build(BuildContext context) {
    final (String label, Color color) = _presentation[kind]!;
    return Semantics(
      label: 'Disponibilité : $label',
      child: Container(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.sm,
          vertical: AppSpacing.xs,
        ),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.12),
          borderRadius: const BorderRadius.all(AppRadius.sm),
          border: Border.all(color: color),
        ),
        child: Text(
          label,
          style: AppTypography.caption.copyWith(
            color: color,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}
