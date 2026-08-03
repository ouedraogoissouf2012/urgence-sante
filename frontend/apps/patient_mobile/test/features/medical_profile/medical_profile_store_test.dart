import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/storage/key_value_store.dart';
import 'package:patient_mobile/features/medical_profile/data/local_medical_profile_store.dart';
import 'package:patient_mobile/features/medical_profile/domain/medical_profile.dart';

/// Faux stockage clé/valeur en mémoire.
class _InMemoryKeyValueStore implements KeyValueStore {
  final Map<String, String> _data = {};

  @override
  Future<String?> read(String key) async => _data[key];

  @override
  Future<void> write(String key, String value) async => _data[key] = value;
}

void main() {
  late _InMemoryKeyValueStore kv;
  late LocalMedicalProfileStore store;

  setUp(() {
    kv = _InMemoryKeyValueStore();
    store = LocalMedicalProfileStore(kv);
  });

  test('une fiche enregistrée est relue à l\'identique (round-trip)', () async {
    const profile = MedicalProfile(
      fullName: 'Awa Koné',
      bloodType: BloodType.oNegative,
      allergies: 'Pénicilline',
      conditions: 'Asthme',
      treatments: 'Ventoline',
      emergencyContactName: 'Koffi',
      emergencyContactPhone: '+2250102030405',
      notes: 'Porte un bracelet médical',
    );

    await store.save(profile);
    final loaded = await store.load();

    expect(loaded.fullName, 'Awa Koné');
    expect(loaded.bloodType, BloodType.oNegative);
    expect(loaded.allergies, 'Pénicilline');
    expect(loaded.conditions, 'Asthme');
    expect(loaded.treatments, 'Ventoline');
    expect(loaded.emergencyContactName, 'Koffi');
    expect(loaded.emergencyContactPhone, '+2250102030405');
    expect(loaded.notes, 'Porte un bracelet médical');
  });

  test('sans fiche enregistrée, on obtient une fiche vide', () async {
    expect((await store.load()).isEmpty, isTrue);
  });

  test('des données corrompues ne font jamais planter : fiche vide', () async {
    await kv.write('medical_profile_v1', 'ceci n\'est pas du JSON {[');
    expect((await store.load()).isEmpty, isTrue);
  });

  test('un groupe sanguin inconnu en base est ignoré, le reste survit', () async {
    await kv.write('medical_profile_v1',
        '{"version":1,"bloodType":"XYZ","allergies":"Iode"}');

    final loaded = await store.load();
    expect(loaded.bloodType, isNull);
    expect(loaded.allergies, 'Iode');
  });

  test('effacer supprime la fiche', () async {
    await store.save(const MedicalProfile(allergies: 'Iode'));
    await store.clear();
    expect((await store.load()).isEmpty, isTrue);
  });
}
