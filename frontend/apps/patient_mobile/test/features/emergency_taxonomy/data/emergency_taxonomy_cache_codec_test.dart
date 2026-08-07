import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/emergency_taxonomy/data/emergency_taxonomy_cache_codec.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/model/emergency_category.dart';

const _categories = [
  EmergencyCategory(
    id: 'respiratoires',
    label: 'Urgences respiratoires',
    directCallOnly: false,
    symptoms: [Symptom(id: 'crise-asthme', label: "Crise d'asthme")],
    serviceCodes: ['pulmonology', 'emergency'],
  ),
  EmergencyCategory(
    id: 'accidents',
    label: 'Accidents et traumatologie',
    directCallOnly: true,
    directCallMessage: 'Appelez immédiatement les secours.',
    symptoms: [Symptom(id: 'fracture', label: 'Fracture')],
    serviceCodes: ['ortho_trauma'],
  ),
];

void main() {
  group('EmergencyTaxonomyCacheCodec', () {
    test('encode puis decode restitue fidèlement la taxonomie', () {
      final decoded = EmergencyTaxonomyCacheCodec.decode(
          EmergencyTaxonomyCacheCodec.encode(_categories));

      expect(decoded, isNotNull);
      expect(decoded, hasLength(2));
      expect(decoded![0].id, 'respiratoires');
      expect(decoded[0].directCallOnly, isFalse);
      expect(decoded[0].directCallMessage, isNull);
      expect(decoded[0].symptoms.single.id, 'crise-asthme');
      expect(decoded[0].symptoms.single.label, "Crise d'asthme");
      expect(decoded[0].serviceCodes, ['pulmonology', 'emergency']);
      expect(decoded[1].directCallOnly, isTrue);
      expect(decoded[1].directCallMessage, 'Appelez immédiatement les secours.');
    });

    test('des données corrompues sont traitées comme cache absent', () {
      expect(EmergencyTaxonomyCacheCodec.decode('pas du json'), isNull);
      expect(EmergencyTaxonomyCacheCodec.decode('{"v":1}'), isNull);
      expect(EmergencyTaxonomyCacheCodec.decode(null), isNull);
      expect(EmergencyTaxonomyCacheCodec.decode(''), isNull);
    });

    test('une version de cache inconnue est ignorée', () {
      expect(EmergencyTaxonomyCacheCodec.decode('{"v":99,"categories":[]}'), isNull);
    });
  });
}
