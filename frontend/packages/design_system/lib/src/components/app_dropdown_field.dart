import 'package:flutter/material.dart';

import '../tokens/app_spacing.dart';
import '../tokens/app_typography.dart';
import 'app_input_decoration.dart';

/// Liste déroulante du design system : même étiquette au-dessus, même fond et
/// mêmes bordures qu'[AppTextField] (décoration partagée), pour pouvoir poser
/// les deux côte à côte (ex. indicatif + numéro de téléphone).
///
/// Sans métier : le libellé de chaque valeur est fourni par l'appelant. Le
/// libellé affiché une fois la liste refermée peut être plus court que celui
/// du menu ([selectedItemLabel]).
class AppDropdownField<T> extends StatelessWidget {
  const AppDropdownField({
    required this.label,
    required this.value,
    required this.items,
    required this.itemLabel,
    required this.onChanged,
    this.selectedItemLabel,
    this.enabled = true,
    super.key,
  });

  final String label;
  final T value;
  final List<T> items;

  /// Libellé d'une valeur dans le menu ouvert.
  final String Function(T item) itemLabel;

  /// Libellé de la valeur choisie, liste refermée (défaut : [itemLabel]).
  final String Function(T item)? selectedItemLabel;
  final ValueChanged<T> onChanged;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    final String Function(T) closedLabel = selectedItemLabel ?? itemLabel;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: AppTypography.buttonLabel),
        const SizedBox(height: AppSpacing.xs),
        DropdownButtonFormField<T>(
          // La valeur pilotée par le parent fait foi : une nouvelle valeur
          // recrée le champ (initialValue n'est lu qu'à la création).
          key: ValueKey<T>(value),
          initialValue: value,
          style: AppTypography.body,
          decoration: appInputDecoration(context),
          onChanged: enabled
              ? (T? selected) {
                  if (selected != null) onChanged(selected);
                }
              : null,
          selectedItemBuilder: (_) => [
            for (final T item in items) Text(closedLabel(item), style: AppTypography.body),
          ],
          items: [
            for (final T item in items)
              DropdownMenuItem<T>(
                value: item,
                child: Text(itemLabel(item), style: AppTypography.body),
              ),
          ],
        ),
      ],
    );
  }
}
