import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../tokens/app_radius.dart';
import '../tokens/app_spacing.dart';
import '../tokens/app_typography.dart';

/// Champ de saisie du design system : étiquette, indice, message d'erreur, et
/// prise en charge des mots de passe (masquage + bascule de visibilité).
///
/// Sans métier : couleurs issues du thème, dimensions issues des tokens. Aucun
/// `Colors.` ni `EdgeInsets` brut (garde d'architecture). Cible tactile de la
/// bascule ≥ 48 dp.
class AppTextField extends StatefulWidget {
  const AppTextField({
    required this.label,
    required this.controller,
    this.hint,
    this.errorText,
    this.obscure = false,
    this.keyboardType,
    this.textInputAction,
    this.inputFormatters,
    this.autofillHints,
    this.onSubmitted,
    this.enabled = true,
    super.key,
  });

  final String label;
  final TextEditingController controller;
  final String? hint;
  final String? errorText;

  /// Masque la saisie (mot de passe) et affiche une bascule de visibilité.
  final bool obscure;
  final TextInputType? keyboardType;
  final TextInputAction? textInputAction;
  final List<TextInputFormatter>? inputFormatters;
  final Iterable<String>? autofillHints;
  final ValueChanged<String>? onSubmitted;
  final bool enabled;

  @override
  State<AppTextField> createState() => _AppTextFieldState();
}

class _AppTextFieldState extends State<AppTextField> {
  late bool _hidden = widget.obscure;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(widget.label, style: AppTypography.buttonLabel),
        const SizedBox(height: AppSpacing.xs),
        TextField(
          controller: widget.controller,
          obscureText: _hidden,
          enabled: widget.enabled,
          keyboardType: widget.keyboardType,
          textInputAction: widget.textInputAction,
          inputFormatters: widget.inputFormatters,
          autofillHints: widget.autofillHints,
          onSubmitted: widget.onSubmitted,
          style: AppTypography.body,
          decoration: InputDecoration(
            hintText: widget.hint,
            errorText: widget.errorText,
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
            suffixIcon: widget.obscure
                ? IconButton(
                    tooltip: _hidden ? 'Afficher le mot de passe' : 'Masquer le mot de passe',
                    icon: Icon(_hidden ? Icons.visibility_off : Icons.visibility),
                    onPressed: () => setState(() => _hidden = !_hidden),
                  )
                : null,
          ),
        ),
      ],
    );
  }
}
