import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/medical_profile/data/local_medical_profile_store.dart';
import 'package:patient_mobile/features/medical_profile/domain/medical_profile.dart';
import 'package:patient_mobile/features/medical_profile/presentation/medical_profile_state.dart';
import 'package:patient_mobile/features/medical_profile/presentation/medical_profile_view_model.dart';

import '../auth/support/auth_fakes.dart' show InMemoryKeyValueStore;

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
}
