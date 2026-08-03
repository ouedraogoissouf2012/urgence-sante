import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/medical_profile/domain/medical_profile.dart';

void main() {
  group('MedicalProfile', () {
    test('une fiche sans aucune donnée est vide', () {
      expect(const MedicalProfile().isEmpty, isTrue);
      expect(const MedicalProfile().isNotEmpty, isFalse);
    });

    test('une fiche avec au moins un champ renseigné n\'est pas vide', () {
      expect(const MedicalProfile(bloodType: BloodType.oPositive).isEmpty, isFalse);
      expect(const MedicalProfile(allergies: 'Pénicilline').isNotEmpty, isTrue);
      expect(
          const MedicalProfile(emergencyContactPhone: '+2250102030405').isNotEmpty,
          isTrue);
    });

    test('les espaces seuls ne comptent pas comme une donnée', () {
      expect(const MedicalProfile(allergies: '   ').isEmpty, isTrue);
    });

    test('copyWith remplace un champ sans toucher aux autres', () {
      const base = MedicalProfile(
        fullName: 'Awa Koné',
        bloodType: BloodType.aPositive,
        allergies: 'Arachides',
      );

      final updated = base.copyWith(allergies: 'Arachides, lactose');

      expect(updated.fullName, 'Awa Koné');
      expect(updated.bloodType, BloodType.aPositive);
      expect(updated.allergies, 'Arachides, lactose');
    });

    test('copyWith peut effacer un champ (clear)', () {
      const base = MedicalProfile(bloodType: BloodType.bNegative);
      expect(base.copyWith(clearBloodType: true).bloodType, isNull);
    });
  });

  group('BloodType', () {
    test('chaque groupe a un libellé lisible', () {
      expect(BloodType.oNegative.label, 'O−');
      expect(BloodType.aPositive.label, 'A+');
      expect(BloodType.abPositive.label, 'AB+');
    });

    test('se convertit vers/depuis un identifiant stable pour le stockage', () {
      for (final type in BloodType.values) {
        expect(BloodType.fromId(type.id), type);
      }
      expect(BloodType.fromId('inconnu'), isNull);
      expect(BloodType.fromId(null), isNull);
    });
  });
}
