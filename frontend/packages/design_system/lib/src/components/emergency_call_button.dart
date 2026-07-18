import 'package:flutter/material.dart';

import '../tokens/app_colors.dart';
import '../tokens/app_spacing.dart';

/// Bouton d'appel d'urgence (SAMU 185, Pompiers 180).
///
/// Le design system ne déclenche pas l'appel lui-même (aucune dépendance
/// technique) : l'application fournit [onPressed]. Cible tactile large et
/// libellé sémantique pour les lecteurs d'écran.
class EmergencyCallButton extends StatelessWidget {
  const EmergencyCallButton({
    required this.label,
    required this.phoneNumber,
    required this.onPressed,
    super.key,
  });

  /// Nom du service appelé (ex. « SAMU »).
  final String label;

  /// Numéro affiché (ex. « 185 »).
  final String phoneNumber;

  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      label: 'Appeler le $label au $phoneNumber',
      child: FilledButton.icon(
        onPressed: onPressed,
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.emergencyCall,
          foregroundColor: Colors.white,
          minimumSize: const Size(120, 56),
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.md,
          ),
        ),
        icon: const Icon(Icons.phone),
        label: Text('$label $phoneNumber'),
      ),
    );
  }
}
