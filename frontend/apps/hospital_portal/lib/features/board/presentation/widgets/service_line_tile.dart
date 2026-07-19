import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../domain/model/service_line.dart';

/// Tuile d'un service : statut courant, horodatage, sélection d'un nouveau
/// statut et accès à l'historique. Aucune logique métier (callbacks fournis).
class ServiceLineTile extends StatelessWidget {
  const ServiceLineTile({
    required this.line,
    required this.updating,
    required this.onStatusSelected,
    required this.onHistoryRequested,
    super.key,
  });

  final ServiceLine line;
  final bool updating;
  final ValueChanged<String> onStatusSelected;
  final VoidCallback onHistoryRequested;

  static const List<(String, String)> _choices = [
    ('AVAILABLE', 'Disponible'),
    ('LIMITED', 'Limité'),
    ('SATURATED', 'Saturé'),
    ('CLOSED', 'Fermé'),
  ];

  String get _updatedLabel {
    final DateTime? updatedAt = line.updatedAt;
    if (updatedAt == null) {
      return 'Jamais renseigné';
    }
    final String time = DateFormat('dd/MM/yyyy HH:mm').format(updatedAt.toLocal());
    return 'Mis à jour le $time';
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(child: Text(line.label, style: AppTypography.title)),
                const SizedBox(width: AppSpacing.sm),
                // Badge à largeur bornée : repli du texte au lieu du débordement.
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 160),
                  child: StatusBadge.fromApi(line.status),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(_updatedLabel, style: AppTypography.caption),
            const SizedBox(height: AppSpacing.sm),
            Wrap(
              spacing: AppSpacing.sm,
              runSpacing: AppSpacing.xs,
              children: [
                for (final (value, label) in _choices)
                  Semantics(
                    button: true,
                    selected: line.status == value,
                    label: 'Marquer ${line.label} : $label',
                    child: ChoiceChip(
                      label: Text(label),
                      selected: line.status == value,
                      onSelected: updating ? null : (_) => onStatusSelected(value),
                    ),
                  ),
              ],
            ),
            Align(
              alignment: Alignment.centerRight,
              child: Semantics(
                button: true,
                label: 'Historique de ${line.label}',
                child: TextButton.icon(
                  onPressed: onHistoryRequested,
                  icon: const Icon(Icons.history),
                  label: const Text('Historique'),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
