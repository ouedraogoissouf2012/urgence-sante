import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/model/medical_need.dart';

/// Sélecteur du besoin médical (puces du catalogue).
class NeedSelector extends StatelessWidget {
  const NeedSelector({
    required this.needs,
    required this.onSelected,
    this.selected,
    super.key,
  });

  final List<MedicalNeed> needs;
  final MedicalNeed? selected;
  final ValueChanged<MedicalNeed> onSelected;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: AppSpacing.sm,
      runSpacing: AppSpacing.sm,
      children: [
        for (final need in needs)
          ChoiceChip(
            label: Text(need.label),
            selected: need == selected,
            onSelected: (_) => onSelected(need),
          ),
      ],
    );
  }
}
