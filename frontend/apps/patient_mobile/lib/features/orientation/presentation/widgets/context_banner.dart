import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

/// Tonalité d'un bandeau contextuel de l'écran d'orientation.
enum ContextBannerTone { offline, approximate }

/// Bandeau d'information contextuelle (hors-ligne, position approximative).
///
/// La couleur provient des tokens de statut du design system ; l'information
/// reste portée par le texte (jamais la couleur seule — accessibilité).
class ContextBanner extends StatelessWidget {
  const ContextBanner({
    required this.tone,
    required this.message,
    super.key,
  });

  final ContextBannerTone tone;
  final String message;

  Color get _accent => switch (tone) {
        ContextBannerTone.offline => AppColors.statusSaturated,
        ContextBannerTone.approximate => AppColors.statusLimited,
      };

  IconData get _icon => switch (tone) {
        ContextBannerTone.offline => Icons.cloud_off,
        ContextBannerTone.approximate => Icons.my_location,
      };

  @override
  Widget build(BuildContext context) {
    final Color accent = _accent;
    return Container(
      padding: const EdgeInsets.all(AppSpacing.sm),
      decoration: BoxDecoration(
        color: accent.withValues(alpha: 0.12),
        borderRadius: AppRadius.card,
        border: Border.all(color: accent.withValues(alpha: 0.4)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(_icon, size: AppSizing.iconMarker, color: accent),
          const SizedBox(width: AppSpacing.sm),
          Expanded(child: Text(message, style: AppTypography.caption)),
        ],
      ),
    );
  }
}
