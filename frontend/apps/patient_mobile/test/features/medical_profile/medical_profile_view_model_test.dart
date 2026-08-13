import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/medical_profile/data/local_medical_profile_store.dart';
import 'package:patient_mobile/features/medical_profile/domain/medical_profile.dart';
import 'package:patient_mobile/features/medical_profile/domain/profile_store.dart';
import 'package:patient_mobile/features/medical_profile/presentation/medical_profile_state.dart';
import 'package:patient_mobile/features/medical_profile/presentation/medical_profile_view_model.dart';

import '../auth/support/auth_fakes.dart' show InMemoryKeyValueStore;

/// Écriture qui échoue toujours, pour vérifier qu'un échec n'est jamais
/// silencieux (issue #140).
class _ThrowingProfileStore implements ProfileStore {
  const _ThrowingProfileStore();

  @override
  Future<MedicalProfile> load() async => const MedicalProfile();

  @override
  Future<void> save(MedicalProfile profile) async =>
      throw Exception('écriture impossible (simulée)');

  @override
  Future<void> clear() async {}
}

void main() {
  late InMemoryKeyValueStore kv;
  late ProviderContainer container;

  setUp(() {
    kv = InMemoryKeyValueStore();
    container = ProviderContainer(overrides: [
      profileStoreProvider.overrideWithValue(
          LocalMedicalProfileStore(kv, InMemoryKeyValueStore())),
    ]);
    addTearDown(container.dispose);
  });

  Future<void> flush() => Future<void>.delayed(Duration.zero);
  MedicalProfileState state() => container.read(medicalProfileViewModelProvider);
  MedicalProfileViewModel vm() =>
      container.read(medicalProfileViewModelProvider.notifier);

  test('au démarrage, charge une fiche vide', () async {
    vm();
    await flush();
    expect(state().phase, MedicalProfilePhase.ready);
    expect(state().profile.isEmpty, isTrue);
  });

  test('enregistrer persiste la fiche et met l\'état à jour', () async {
    vm();
    await flush();

    await vm().save(const MedicalProfile(
      bloodType: BloodType.oNegative,
      allergies: 'Pénicilline',
    ));

    expect(state().profile.bloodType, BloodType.oNegative);
    expect(state().profile.allergies, 'Pénicilline');
    // Persisté : un nouveau view-model relit la même fiche.
    final other = ProviderContainer(overrides: [
      profileStoreProvider.overrideWithValue(
          LocalMedicalProfileStore(kv, InMemoryKeyValueStore())),
    ]);
    addTearDown(other.dispose);
    other.read(medicalProfileViewModelProvider.notifier);
    await flush();
    expect(other.read(medicalProfileViewModelProvider).profile.allergies,
        'Pénicilline');
  });

  test('effacer vide la fiche', () async {
    vm();
    await flush();
    await vm().save(const MedicalProfile(allergies: 'Iode'));

    await vm().clear();

    expect(state().profile.isEmpty, isTrue);
  });

  test(
      'un échec d\'écriture renvoie false et ne met PAS à jour la fiche affichée',
      () async {
    final failingContainer = ProviderContainer(overrides: [
      profileStoreProvider.overrideWithValue(const _ThrowingProfileStore()),
    ]);
    addTearDown(failingContainer.dispose);
    failingContainer.read(medicalProfileViewModelProvider.notifier);
    await flush();

    final bool saved = await failingContainer
        .read(medicalProfileViewModelProvider.notifier)
        .save(const MedicalProfile(allergies: 'Iode'));

    expect(saved, isFalse);
    // La fiche affichée reste celle d'avant l'échec (pas de fausse impression
    // que "Iode" a été enregistré).
    expect(failingContainer.read(medicalProfileViewModelProvider).profile.allergies,
        isNull);
  });
}
