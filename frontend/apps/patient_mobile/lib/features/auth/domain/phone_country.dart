/// Pays proposé pour l'indicatif téléphonique, avec les règles de son plan de
/// numérotation national. Donnée métier pure (aucune dépendance Flutter).
class PhoneCountry {
  const PhoneCountry({
    required this.isoCode,
    required this.name,
    required this.flag,
    required this.dialCode,
    required this.nationalLength,
    required this.groups,
    required this.example,
    this.trunkPrefix,
    this.nationalPattern,
    this.invalidPrefixMessage,
  });

  /// Code ISO 3166-1 alpha-2 (identité du pays).
  final String isoCode;
  final String name;
  final String flag;

  /// Indicatif international, chiffres seuls (ex. « 225 »).
  final String dialCode;

  /// Nombre de chiffres du numéro national une fois le préfixe national retiré.
  final int nationalLength;

  /// Taille des groupes de chiffres à l'affichage (ex. paires en Côte d'Ivoire).
  final List<int> groups;

  /// Exemple de saisie, affiché en indice et dans les messages d'erreur.
  final String example;

  /// Préfixe national tapé localement mais absent du format international
  /// (le « 0 » français ou ghanéen). Nul si le 0 fait partie du numéro.
  final String? trunkPrefix;

  /// Motif du numéro national, si le plan de numérotation est contrôlé.
  final String? nationalPattern;
  final String? invalidPrefixMessage;

  String get displayDialCode => '+$dialCode';

  /// Chiffres qu'un utilisateur peut taper localement (préfixe national inclus).
  int get maxTypedDigits => nationalLength + (trunkPrefix?.length ?? 0);

  @override
  bool operator ==(Object other) => other is PhoneCountry && other.isoCode == isoCode;

  @override
  int get hashCode => isoCode.hashCode;
}

/// Indicatifs proposés : Côte d'Ivoire par défaut (marché du Lot 1), puis les
/// pays voisins et la diaspora les plus fréquents parmi les habitants
/// d'Abidjan. Le serveur reste juge du format final (E.164).
abstract final class PhoneCountries {
  /// Plan ivoirien de 2021 : 10 chiffres, le 0 initial fait partie du numéro ;
  /// 01/05/07 mobiles, 21/25/27 fixes.
  static const PhoneCountry ivoryCoast = PhoneCountry(
    isoCode: 'CI',
    name: "Côte d'Ivoire",
    flag: '🇨🇮',
    dialCode: '225',
    nationalLength: 10,
    groups: [2, 2, 2, 2, 2],
    example: '05 44 21 01 12',
    nationalPattern: r'^(01|05|07|21|25|27)\d{8}$',
    invalidPrefixMessage:
        'Numéro ivoirien invalide : il commence par 01, 05, 07, 21, 25 ou 27.',
  );

  static const PhoneCountry burkinaFaso = PhoneCountry(
    isoCode: 'BF',
    name: 'Burkina Faso',
    flag: '🇧🇫',
    dialCode: '226',
    nationalLength: 8,
    groups: [2, 2, 2, 2],
    example: '70 12 34 56',
  );

  static const PhoneCountry mali = PhoneCountry(
    isoCode: 'ML',
    name: 'Mali',
    flag: '🇲🇱',
    dialCode: '223',
    nationalLength: 8,
    groups: [2, 2, 2, 2],
    example: '76 12 34 56',
  );

  static const PhoneCountry ghana = PhoneCountry(
    isoCode: 'GH',
    name: 'Ghana',
    flag: '🇬🇭',
    dialCode: '233',
    nationalLength: 9,
    groups: [3, 3, 4],
    example: '024 123 4567',
    trunkPrefix: '0',
  );

  static const PhoneCountry guinea = PhoneCountry(
    isoCode: 'GN',
    name: 'Guinée',
    flag: '🇬🇳',
    dialCode: '224',
    nationalLength: 9,
    groups: [3, 2, 2, 2],
    example: '622 12 34 56',
  );

  static const PhoneCountry senegal = PhoneCountry(
    isoCode: 'SN',
    name: 'Sénégal',
    flag: '🇸🇳',
    dialCode: '221',
    nationalLength: 9,
    groups: [2, 3, 2, 2],
    example: '77 123 45 67',
  );

  static const PhoneCountry france = PhoneCountry(
    isoCode: 'FR',
    name: 'France',
    flag: '🇫🇷',
    dialCode: '33',
    nationalLength: 9,
    groups: [2, 2, 2, 2, 2],
    example: '06 12 34 56 78',
    trunkPrefix: '0',
  );

  static const PhoneCountry defaultCountry = ivoryCoast;

  static const List<PhoneCountry> supported = [
    ivoryCoast,
    burkinaFaso,
    mali,
    ghana,
    guinea,
    senegal,
    france,
  ];
}
