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
  late _InMemoryKeyValueStore secureKv;
  late _InMemoryKeyValueStore legacyKv;
  late LocalMedicalProfileStore store;

  setUp(() {
    secureKv = _InMemoryKeyValueStore();
    legacyKv = _InMemoryKeyValueStore();
    store = LocalMedicalProfileStore(secureKv, legacyKv);
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
    await secureKv.write('medical_profile_v1', 'ceci n\'est pas du JSON {[');
    expect((await store.load()).isEmpty, isTrue);
  });

  test('un groupe sanguin inconnu en base est ignoré, le reste survit', () async {
    await secureKv.write('medical_profile_v1',
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

  test('save() écrit dans le stockage chiffré, jamais dans l\'ancien stockage en clair',
      () async {
    await store.save(const MedicalProfile(allergies: 'Iode'));

    expect(secureKv._data['medical_profile_v1'], isNotNull);
    expect(legacyKv._data['medical_profile_v1'], isNull);
  });

  test(
      'une fiche en clair d\'une version antérieure (#128) est reprise (migration) '
      'puis effacée du stockage non chiffré', () async {
    await legacyKv.write('medical_profile_v1',
        '{"version":1,"fullName":"Awa Koné","allergies":"Pénicilline"}');

    final loaded = await store.load();

    expect(loaded.fullName, 'Awa Koné');
    expect(loaded.allergies, 'Pénicilline');
    // Reprise dans le stockage chiffré...
    expect(secureKv._data['medical_profile_v1'], isNotNull);
    // ...et effacée de l'ancien stockage en clair (ne doit plus y traîner).
    expect(legacyKv._data['medical_profile_v1'], '');
  });

  test('une fiche déjà chiffrée n\'est jamais reprise depuis l\'ancien stockage en clair',
      () async {
    await secureKv.write(
        'medical_profile_v1', '{"version":1,"fullName":"Chiffrée"}');
    await legacyKv.write(
        'medical_profile_v1', '{"version":1,"fullName":"En clair (obsolète)"}');

    final loaded = await store.load();

    expect(loaded.fullName, 'Chiffrée');
    // L'ancien stockage en clair n'est pas touché quand une fiche chiffrée existe déjà.
    expect(legacyKv._data['medical_profile_v1'], '{"version":1,"fullName":"En clair (obsolète)"}');
  });

  test('sans fiche chiffrée ni fiche en clair, on obtient une fiche vide sans écrire nulle part',
      () async {
    expect((await store.load()).isEmpty, isTrue);
    expect(secureKv._data.containsKey('medical_profile_v1'), isFalse);
    expect(legacyKv._data.containsKey('medical_profile_v1'), isFalse);
  });

  group('migration de clé (issue #140 — futur bump de schéma)', () {
    test(
        'une fiche trouvée sous une clé plus ancienne est reprise sous la clé courante',
        () async {
      // keysNewestFirst n'est fourni qu'en test : simule "aujourd'hui, une
      // clé plus ancienne que medical_profile_v1 existe encore".
      final storeWithHistory = LocalMedicalProfileStore(
        secureKv,
        legacyKv,
        keysNewestFirst: const ['medical_profile_v1', 'medical_profile_v0'],
      );
      await secureKv.write(
          'medical_profile_v0', '{"version":1,"fullName":"Ancienne fiche"}');

      final loaded = await storeWithHistory.load();

      expect(loaded.fullName, 'Ancienne fiche');
      // Reprise sous la clé courante : les données ne restent pas orphelines
      // sous l'ancienne clé, une lecture normale (sans historique étendu) les
      // retrouve désormais aussi.
      expect(secureKv._data['medical_profile_v1'], isNotNull);
      expect((await store.load()).fullName, 'Ancienne fiche');
      // L'ancienne clé ne traîne plus une fois la fiche reprise.
      expect(secureKv._data['medical_profile_v0'], '');
    });

    test(
        'une fiche déjà sous la clé courante n\'est jamais recopiée depuis une clé plus ancienne',
        () async {
      final storeWithHistory = LocalMedicalProfileStore(
        secureKv,
        legacyKv,
        keysNewestFirst: const ['medical_profile_v1', 'medical_profile_v0'],
      );
      await secureKv.write(
          'medical_profile_v1', '{"version":1,"fullName":"Fiche courante"}');
      await secureKv.write(
          'medical_profile_v0', '{"version":1,"fullName":"Fiche obsolète"}');

      final loaded = await storeWithHistory.load();

      expect(loaded.fullName, 'Fiche courante');
    });

    test(
        'sans liste de clés historiques étendue (comportement par défaut), '
        'une clé plus ancienne inconnue n\'est jamais consultée', () async {
      await secureKv.write(
          'medical_profile_v0', '{"version":1,"fullName":"Ancienne fiche"}');

      // `store` par défaut : keysNewestFirst réel de production, une seule clé.
      expect((await store.load()).isEmpty, isTrue);
    });
  });
}
