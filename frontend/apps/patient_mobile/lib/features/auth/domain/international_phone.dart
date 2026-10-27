import 'phone_country.dart';

/// Résultat de la composition d'un numéro : soit le numéro international
/// (E.164, attendu par le serveur), soit le message à afficher sous le champ.
class PhoneValidation {
  const PhoneValidation._({this.e164, this.error});

  const PhoneValidation.valid(String e164) : this._(e164: e164);
  const PhoneValidation.invalid(String error) : this._(error: error);

  final String? e164;
  final String? error;
}

final RegExp _nonDigits = RegExp(r'\D');

bool _hasInternationalPrefix(String raw) {
  final String trimmed = raw.trimLeft();
  return trimmed.startsWith('+') || trimmed.startsWith('00');
}

/// Chiffres saisis après l'éventuel préfixe international (« + » ou « 00 »).
String _internationalDigits(String raw) {
  final String digits = raw.replaceAll(_nonDigits, '');
  return raw.trimLeft().startsWith('00') ? digits.substring(2) : digits;
}

/// Chiffres nationaux en cours de saisie : l'indicatif du pays, tapé ou collé
/// en tête (« +225… », « 00225… », ou « 225… » dès qu'il dépasse la longueur
/// d'un numéro national), est retiré ; le résultat est borné au nombre de
/// chiffres tapables du pays. Le préfixe national (0) éventuel est conservé :
/// c'est ce que l'utilisateur a l'habitude de voir.
String typedNationalDigits(PhoneCountry country, String raw) {
  String digits = raw.replaceAll(_nonDigits, '');
  if (_hasInternationalPrefix(raw)) {
    final String international = _internationalDigits(raw);
    if (international.startsWith(country.dialCode)) {
      digits = international.substring(country.dialCode.length);
    }
  } else if (digits.length > country.maxTypedDigits &&
      digits.startsWith(country.dialCode)) {
    digits = digits.substring(country.dialCode.length);
  }
  return digits.length > country.maxTypedDigits
      ? digits.substring(0, country.maxTypedDigits)
      : digits;
}

/// Groupe les chiffres selon l'usage du pays (« 05 44 21 01 12 »). Une saisie
/// partielle affiche ses groupes complets puis le reste.
String formatNationalNumber(PhoneCountry country, String digits) {
  final List<String> parts = [];
  int start = 0;
  for (final int size in country.groups) {
    if (start >= digits.length) break;
    final int end = (start + size).clamp(0, digits.length);
    parts.add(digits.substring(start, end));
    start = end;
  }
  if (start < digits.length) parts.add(digits.substring(start));
  return parts.join(' ');
}

/// Compose le numéro international à partir de l'indicatif choisi et de la
/// saisie locale — y compris une saisie qui reprend l'indicatif (« 225 05… »,
/// « +225 05… ») — et le valide selon le plan de numérotation du pays.
PhoneValidation validateInternationalPhone(PhoneCountry country, String raw) {
  if (raw.trim().isEmpty) {
    return const PhoneValidation.invalid('Le numéro de téléphone est requis.');
  }
  String national;
  if (_hasInternationalPrefix(raw)) {
    final String international = _internationalDigits(raw);
    if (!international.startsWith(country.dialCode)) {
      return const PhoneValidation.invalid(
          'Ce numéro commence par un autre indicatif : choisissez-le dans « Code ».');
    }
    national = international.substring(country.dialCode.length);
  } else {
    national = raw.replaceAll(_nonDigits, '');
    if (national.length > country.maxTypedDigits &&
        national.startsWith(country.dialCode)) {
      national = national.substring(country.dialCode.length);
    }
  }

  final String? trunk = country.trunkPrefix;
  if (trunk != null &&
      national.length == country.nationalLength + trunk.length &&
      national.startsWith(trunk)) {
    national = national.substring(trunk.length);
  }

  if (national.length != country.nationalLength) {
    return PhoneValidation.invalid(trunk == null
        ? 'Le numéro doit comporter ${country.nationalLength} chiffres '
            '(ex. ${country.example}).'
        : 'Numéro invalide pour ${country.name} (ex. ${country.example}).');
  }
  final String? pattern = country.nationalPattern;
  if (pattern != null && !RegExp(pattern).hasMatch(national)) {
    return PhoneValidation.invalid(
        country.invalidPrefixMessage ?? 'Numéro invalide (ex. ${country.example}).');
  }
  return PhoneValidation.valid('+${country.dialCode}$national');
}
