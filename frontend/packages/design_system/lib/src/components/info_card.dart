import 'package:flutter/material.dart';

import '../tokens/app_radius.dart';
import '../tokens/app_sizing.dart';
import '../tokens/app_spacing.dart';
import '../tokens/app_typography.dart';

/// Tonalité d'une carte d'information.
enum InfoCardTone {
  /// Information neutre (contexte, réassurance).
  neutral,

  /// Avertissement important (limites, sécurité).
  alert,
}

/// Carte d'information avec titre, corps et icône, déclinée en deux tonalités.
///
/// Sans métier : couleurs et tailles proviennent des tokens du design system.
/// La tonalité `alert` emprunte la couleur d'erreur du thème pour signaler une
/// information critique (jamais la couleur seule : titre explicite + icône).
class InfoCard extends StatelessWidget {
  const InfoCard({
    required this.icon,
    required this.title,
    required this.body,
    this.tone = InfoCardTone.neutral,
    super.key,
  });

  final IconData icon;
  final String title;
  final String body;
  final InfoCardTone tone;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    final Color accent =
        tone == InfoCardTone.alert ? scheme.error : scheme.primary;
    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: AppRadius.cardLarge,
        side: BorderSide(color: accent.withValues(alpha: 0.35)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: accent, size: AppSizing.iconMarker),
            const SizedBox(width: AppSpacing.md),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: AppTypography.title.copyWith(color: accent),
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(body, style: AppTypography.body),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
