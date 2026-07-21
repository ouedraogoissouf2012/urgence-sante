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

  // Présentation d'un statut : libellé, teinte d'identité (fond + bordure),
  // et les deux couleurs de texte accessibles (clair/sombre). Le badge choisit
  // la variante de texte selon la luminosité du thème. Voir `AppColors`.
  static const Map<StatusBadgeKind, _BadgeStyle> _presentation = {
    StatusBadgeKind.available: _BadgeStyle(
      'Disponible',
      AppColors.statusAvailable,
      AppColors.statusAvailableTextLight,
      AppColors.statusAvailableTextDark,
    ),
    StatusBadgeKind.limited: _BadgeStyle(
      'Limité',
      AppColors.statusLimited,
      AppColors.statusLimitedTextLight,
      AppColors.statusLimitedTextDark,
    ),
    StatusBadgeKind.saturated: _BadgeStyle(
      'Saturé',
      AppColors.statusSaturated,
      AppColors.statusSaturatedTextLight,
      AppColors.statusSaturatedTextDark,
    ),
    StatusBadgeKind.closed: _BadgeStyle(
      'Fermé',
      AppColors.statusClosed,
      AppColors.statusClosedTextLight,
      AppColors.statusClosedTextDark,
    ),
    StatusBadgeKind.unknown: _BadgeStyle(
      'Non confirmé',
      AppColors.statusUnknown,
      AppColors.statusUnknownTextLight,
      AppColors.statusUnknownTextDark,
    ),
  };

  @override
  Widget build(BuildContext context) {
    final _BadgeStyle style = _presentation[kind]!;
    // Texte lisible dans les deux thèmes : la surface du badge suit la
    // luminosité de l'application, donc la couleur du texte aussi.
    final bool isDark = Theme.of(context).brightness == Brightness.dark;
    final Color textColor = isDark ? style.textDark : style.textLight;
    return Semantics(
      label: 'Disponibilité : ${style.label}',
      child: Container(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.sm,
          vertical: AppSpacing.xs,
        ),
        decoration: BoxDecoration(
          // Teinte d'identité pour le fond translucide et la bordure ;
          // le texte, lui, utilise la variante lisible du thème courant.
          color: style.tint.withValues(alpha: 0.12),
          borderRadius: AppRadius.badge,
          border: Border.all(color: style.tint),
        ),
        child: Text(
          style.label,
          style: AppTypography.caption.copyWith(
            color: textColor,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

/// Présentation immuable d'un statut de disponibilité : libellé, teinte
/// d'identité (fond + bordure) et les deux couleurs de texte accessibles
/// (thème clair / thème sombre). Constructeur `const` : la table de
/// présentation reste entièrement constante.
class _BadgeStyle {
  const _BadgeStyle(this.label, this.tint, this.textLight, this.textDark);

  final String label;
  final Color tint;
  final Color textLight;
  final Color textDark;
}
