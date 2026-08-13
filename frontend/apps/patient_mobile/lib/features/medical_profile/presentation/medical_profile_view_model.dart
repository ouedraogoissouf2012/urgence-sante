import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../domain/medical_profile.dart';
import '../domain/profile_store.dart';
import 'medical_profile_state.dart';

/// ViewModel de la fiche médicale (MVVM, flux unidirectionnel).
///
/// Charge la fiche locale au démarrage ; enregistre / efface via le
/// [ProfileStore]. Aucune dépendance widget : testable en pur Dart. Les données
/// ne quittent jamais l'appareil.
final medicalProfileViewModelProvider =
    NotifierProvider<MedicalProfileViewModel, MedicalProfileState>(
  MedicalProfileViewModel.new,
);

class MedicalProfileViewModel extends Notifier<MedicalProfileState> {
  ProfileStore get _store => ref.read(profileStoreProvider);

  @override
  MedicalProfileState build() {
    Future.microtask(_load);
    return const MedicalProfileState();
  }

  Future<void> _load() async {
    final profile = await _store.load();
    state = state.copyWith(phase: MedicalProfilePhase.ready, profile: profile);
  }

  /// Enregistre la fiche sur l'appareil et met l'état à jour.
  ///
  /// Retourne `false` si l'écriture a échoué : l'état n'est alors PAS mis à
  /// jour (l'ancienne fiche reste affichée) et l'appelant doit avertir
  /// l'utilisateur — un échec ne doit jamais être silencieux (issue #140).
  Future<bool> save(MedicalProfile profile) async {
    try {
      await _store.save(profile);
    } on Exception {
      return false;
    }
    state = state.copyWith(phase: MedicalProfilePhase.ready, profile: profile);
    return true;
  }

  /// Efface la fiche de l'appareil.
  Future<void> clear() async {
    await _store.clear();
    state = state.copyWith(
        phase: MedicalProfilePhase.ready, profile: const MedicalProfile());
  }
}
