import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../di/providers.dart';

/// Barre d'appels d'urgence, accessible en permanence (SAMU 185, Pompiers 180).
class EmergencyCallBar extends ConsumerWidget {
  const EmergencyCallBar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final caller = ref.watch(emergencyCallerProvider);
    return SafeArea(
      top: false,
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.sm),
        child: Row(
          children: [
            Expanded(
              child: EmergencyCallButton(
                label: 'SAMU',
                phoneNumber: '185',
                onPressed: () => caller.call('185'),
              ),
            ),
            const SizedBox(width: AppSpacing.sm),
            Expanded(
              child: EmergencyCallButton(
                label: 'Pompiers',
                phoneNumber: '180',
                onPressed: () => caller.call('180'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
