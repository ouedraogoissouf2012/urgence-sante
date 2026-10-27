import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../../domain/phone_country.dart';
import 'national_number_formatter.dart';

/// Saisie du téléphone en deux champs : indicatif (« Code », Côte d'Ivoire par
/// défaut) et numéro national mis en forme pendant la frappe. L'utilisateur ne
/// tape que ce qu'il connaît (« 05 44 21 01 12 ») ; la composition du numéro
/// international est faite par le domaine (validateInternationalPhone).
///
/// Champ contrôlé : l'indicatif choisi appartient au parent.
class PhoneNumberField extends StatelessWidget {
  const PhoneNumberField({
    required this.country,
    required this.controller,
    required this.onCountryChanged,
    this.errorText,
    this.enabled = true,
    this.textInputAction,
    super.key,
  });

  final PhoneCountry country;
  final TextEditingController controller;
  final ValueChanged<PhoneCountry> onCountryChanged;
  final String? errorText;
  final bool enabled;
  final TextInputAction? textInputAction;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Largeur du contenu (« 🇨🇮 +225 ») : aucune dimension codée en dur.
        IntrinsicWidth(
          child: AppDropdownField<PhoneCountry>(
            label: 'Code',
            value: country,
            items: PhoneCountries.supported,
            enabled: enabled,
            itemLabel: (c) => '${c.flag} ${c.name}  ${c.displayDialCode}',
            selectedItemLabel: (c) => '${c.flag} ${c.displayDialCode}',
            onChanged: onCountryChanged,
          ),
        ),
        const SizedBox(width: AppSpacing.sm),
        Expanded(
          child: AppTextField(
            label: 'Téléphone',
            hint: country.example,
            controller: controller,
            enabled: enabled,
            errorText: errorText,
            keyboardType: TextInputType.phone,
            textInputAction: textInputAction,
            autofillHints: const [AutofillHints.telephoneNumber],
            inputFormatters: [NationalNumberFormatter(country)],
          ),
        ),
      ],
    );
  }
}
