import 'package:api_client/api.dart' as api;
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/emergency_taxonomy/data/emergency_taxonomy_mapper.dart';

void main() {
  group('EmergencyTaxonomyMapper', () {
    test('traduit les catégories générées vers le domaine', () {
      final domain = EmergencyTaxonomyMapper.toDomain([
        api.EmergencyCategory(
          id: 'respiratoires',
          label: 'Urgences respiratoires',
          directCallOnly: false,
          symptoms: [api.Symptom(id: 'crise-asthme', label: "Crise d'asthme")],
          serviceCodes: ['pulmonology', 'emergency'],
        ),
        api.EmergencyCategory(
          id: 'accidents',
          label: 'Accidents et traumatologie',
          directCallOnly: true,
          directCallMessage: 'Appelez les secours.',
          symptoms: [api.Symptom(id: 'fracture', label: 'Fracture')],
          serviceCodes: ['ortho_trauma', 'emergency'],
        ),
      ]);

      expect(domain, hasLength(2));

      final respiratoires = domain[0];
      expect(respiratoires.id, 'respiratoires');
      expect(respiratoires.directCallOnly, isFalse);
      expect(respiratoires.directCallMessage, isNull);
      expect(respiratoires.symptoms.single.id, 'crise-asthme');
      expect(respiratoires.symptoms.single.label, "Crise d'asthme");
      expect(respiratoires.serviceCodes, ['pulmonology', 'emergency']);

      final accidents = domain[1];
      expect(accidents.directCallOnly, isTrue);
      expect(accidents.directCallMessage, 'Appelez les secours.');
    });

    test('une liste vide donne une taxonomie vide', () {
      expect(EmergencyTaxonomyMapper.toDomain(const []), isEmpty);
    });
  });
}
