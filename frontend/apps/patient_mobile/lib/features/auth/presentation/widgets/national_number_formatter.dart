import 'package:flutter/services.dart';

import '../../domain/international_phone.dart';
import '../../domain/phone_country.dart';

/// Met en forme le numéro national pendant la saisie : chiffres seuls, bornés
/// à la longueur du pays, groupés à son usage (« 05 44 21 01 12 »), indicatif
/// tapé ou collé retiré. Les règles viennent du domaine ; ce formateur ne fait
/// que les appliquer au champ et replacer le curseur.
class NationalNumberFormatter extends TextInputFormatter {
  NationalNumberFormatter(this.country);

  final PhoneCountry country;

  static final RegExp _digit = RegExp(r'\d');

  @override
  TextEditingValue formatEditUpdate(TextEditingValue oldValue, TextEditingValue newValue) {
    String raw = newValue.text;
    final int cursor = newValue.selection.end.clamp(0, raw.length);
    int digitsBefore = _countDigits(raw.substring(0, cursor));

    // Retour arrière sur un espace de groupement : sans ceci, l'espace serait
    // aussitôt remis et l'effacement semblerait bloqué. On efface le chiffre
    // qui le précède, comme l'utilisateur l'attend.
    final bool removedSeparatorOnly = oldValue.text.length == raw.length + 1 &&
        _countDigits(oldValue.text) == _countDigits(raw) &&
        digitsBefore > 0;
    if (removedSeparatorOnly) {
      final List<String> chars = raw.replaceAll(RegExp(r'\D'), '').split('')
        ..removeAt(digitsBefore - 1);
      raw = chars.join();
      digitsBefore -= 1;
    }

    final String digits = typedNationalDigits(country, raw);
    final String formatted = formatNationalNumber(country, digits);

    // Curseur : même nombre de chiffres à sa gauche qu'avant mise en forme.
    // Si un indicatif a été retiré, la correspondance n'a plus de sens : fin.
    final int rawDigits = _countDigits(raw);
    final int offset = rawDigits != digits.length
        ? formatted.length
        : _offsetAfterDigits(formatted, digitsBefore);

    return TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(offset: offset),
    );
  }

  static int _countDigits(String text) => _digit.allMatches(text).length;

  static int _offsetAfterDigits(String formatted, int digits) {
    if (digits == 0) return 0;
    int seen = 0;
    for (int i = 0; i < formatted.length; i++) {
      if (_digit.hasMatch(formatted[i]) && ++seen == digits) return i + 1;
    }
    return formatted.length;
  }
}
