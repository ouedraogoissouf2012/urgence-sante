import 'package:flutter/material.dart';

import '../tokens/app_radius.dart';

/// Décoration commune des champs de saisie du design system (texte et liste
/// déroulante) : un champ de texte et une liste posés côte à côte ont ainsi
/// exactement le même fond, les mêmes bordures et la même hauteur.
///
/// Couleurs issues du thème, rayons issus des tokens.
InputDecoration appInputDecoration(
  BuildContext context, {
  String? hint,
  String? errorText,
  Widget? suffixIcon,
}) {
  final ColorScheme scheme = Theme.of(context).colorScheme;
  return InputDecoration(
    hintText: hint,
    errorText: errorText,
    filled: true,
    fillColor: scheme.surfaceContainerHighest,
    border: OutlineInputBorder(
      borderRadius: AppRadius.card,
      borderSide: BorderSide(color: scheme.outline),
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: AppRadius.card,
      borderSide: BorderSide(color: scheme.outlineVariant),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: AppRadius.card,
      borderSide: BorderSide(color: scheme.primary, width: 2),
    ),
    suffixIcon: suffixIcon,
  );
}
