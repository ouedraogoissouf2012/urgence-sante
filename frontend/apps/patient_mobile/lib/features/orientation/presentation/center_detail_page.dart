import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../domain/model/recommended_center.dart';
import 'widgets/center_detail_stats.dart';
import 'widgets/emergency_call_bar.dart';

/// Fiche détail d'un centre recommandé : statut, distance/trajet, raison du
/// classement et actions (itinéraire, appel). Les secours restent accessibles
/// en permanence (barre du bas).
///
/// N'affiche que des données réellement disponibles : la liste des services
/// offerts n'est pas fournie par le contrat d'orientation, elle n'est donc pas
/// inventée (une app d'urgence ne montre jamais d'information non confirmée).
class CenterDetailPage extends ConsumerWidget {
  const CenterDetailPage({required this.center, super.key});

  final RecommendedCenter center;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bool canCall = center.phone != null;
    return Scaffold(
      appBar: AppBar(title: const Text('Fiche du centre')),
      bottomNavigationBar: const EmergencyCallBar(),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          Text(center.name, style: AppTypography.headline),
          const SizedBox(height: AppSpacing.sm),
          StatusBadge.fromApi(center.status),
          const SizedBox(height: AppSpacing.lg),
          CenterDetailStats(center: center),
          const SizedBox(height: AppSpacing.lg),
          const Text('Pourquoi ce centre', style: AppTypography.title),
          const SizedBox(height: AppSpacing.xs),
          Text(center.explanation, style: AppTypography.body),
          const SizedBox(height: AppSpacing.xl),
          FilledButton.icon(
            onPressed: () =>
                ref.read(navigationLauncherProvider).navigateTo(
                      latitude: center.latitude,
                      longitude: center.longitude,
                      label: center.name,
                    ),
            icon: const Icon(Icons.directions),
            label: const Text('Y aller'),
          ),
          const SizedBox(height: AppSpacing.sm),
          Semantics(
            button: true,
            label: canCall
                ? 'Appeler ${center.name}'
                : 'Numéro du centre indisponible',
            child: OutlinedButton.icon(
              onPressed: canCall
                  ? () => ref
                      .read(emergencyCallerProvider)
                      .call(center.phone!)
                  : null,
              icon: const Icon(Icons.call),
              label: Text(canCall ? 'Appeler le centre' : 'Numéro indisponible'),
            ),
          ),
        ],
      ),
    );
  }
}
