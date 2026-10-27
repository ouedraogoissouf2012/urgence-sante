import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/auth/domain/international_phone.dart';
import 'package:patient_mobile/features/auth/domain/phone_country.dart';

void main() {
  const ci = PhoneCountries.ivoryCoast;
  const france = PhoneCountries.france;
  const burkina = PhoneCountries.burkinaFaso;

  String? e164(PhoneCountry country, String raw) =>
      validateInternationalPhone(country, raw).e164;
  String? error(PhoneCountry country, String raw) =>
      validateInternationalPhone(country, raw).error;

  group('liste des indicatifs', () {
    test("la Côte d'Ivoire est l'indicatif par défaut, en tête de liste", () {
      expect(PhoneCountries.defaultCountry, ci);
      expect(PhoneCountries.supported.first, ci);
      expect(ci.displayDialCode, '+225');
    });

    test('la courte liste convenue est proposée, sans doublon', () {
      expect(PhoneCountries.supported.map((c) => c.displayDialCode),
          ['+225', '+226', '+223', '+233', '+224', '+221', '+33']);
    });
  });

  group('composition du numéro international (E.164)', () {
    test("numéro ivoirien saisi localement, avec ou sans espaces", () {
      expect(e164(ci, '0544210112'), '+2250544210112');
      expect(e164(ci, '05 44 21 01 12'), '+2250544210112');
    });

    test('capture terrain : « 225 0544210112 » sans le + est accepté', () {
      expect(e164(ci, '225 0544210112'), '+2250544210112');
    });

    test('numéro collé au format international (+225 ou 00225)', () {
      expect(e164(ci, '+225 05 44 21 01 12'), '+2250544210112');
      expect(e164(ci, '00225 0544210112'), '+2250544210112');
    });

    test('le 0 initial ivoirien fait partie du numéro (plan 2021)', () {
      expect(e164(ci, '0102030405'), '+2250102030405');
    });

    test('le 0 de préfixe national est retiré là où il ne fait pas partie du numéro', () {
      expect(e164(france, '06 12 34 56 78'), '+33612345678');
      expect(e164(france, '612345678'), '+33612345678');
    });

    test('pays à 8 chiffres', () {
      expect(e164(burkina, '70 12 34 56'), '+22670123456');
    });
  });

  group('erreurs affichées sous le champ', () {
    test('numéro vide', () {
      expect(error(ci, '   '), 'Le numéro de téléphone est requis.');
    });

    test('longueur incorrecte : le message donne le bon nombre de chiffres', () {
      expect(error(ci, '05442101'),
          'Le numéro doit comporter 10 chiffres (ex. 05 44 21 01 12).');
      expect(e164(ci, '05442101'), isNull);
    });

    test('préfixe ivoirien impossible', () {
      expect(error(ci, '0944210112'),
          'Numéro ivoirien invalide : il commence par 01, 05, 07, 21, 25 ou 27.');
    });

    test('numéro collé avec un autre indicatif que celui choisi', () {
      expect(error(ci, '+226 70 12 34 56'),
          'Ce numéro commence par un autre indicatif : choisissez-le dans « Code ».');
    });

    test('pays avec préfixe national : message sans nombre de chiffres trompeur', () {
      expect(error(france, '0612'), 'Numéro invalide pour France (ex. 06 12 34 56 78).');
    });
  });

  group('saisie en cours (chiffres nationaux tapés)', () {
    test("l'indicatif tapé en tête est retiré dès qu'il dépasse la longueur", () {
      expect(typedNationalDigits(ci, '22505442101'), '05442101');
      expect(typedNationalDigits(ci, '+2250544'), '0544');
    });

    test('une saisie plus courte que le numéro est conservée telle quelle', () {
      expect(typedNationalDigits(ci, '225'), '225');
    });

    test('la saisie est bornée au nombre de chiffres du pays', () {
      expect(typedNationalDigits(ci, '054421011299'), '0544210112');
      expect(typedNationalDigits(france, '0612345678'), '0612345678');
    });
  });

  group('affichage groupé', () {
    test('Côte d\'Ivoire : paires de chiffres', () {
      expect(formatNationalNumber(ci, '0544210112'), '05 44 21 01 12');
    });

    test('saisie partielle : groupes complets puis reste', () {
      expect(formatNationalNumber(ci, '05442'), '05 44 2');
    });

    test('groupement propre au pays', () {
      expect(formatNationalNumber(PhoneCountries.senegal, '771234567'), '77 123 45 67');
    });
  });
}
