import '../domain/medical_profile.dart';

/// Phase de chargement de la fiche médicale.
enum MedicalProfilePhase { loading, ready }

/// État immuable de la fiche médicale (chargement + contenu courant).
class MedicalProfileState {
  const MedicalProfileState({
    this.phase = MedicalProfilePhase.loading,
    this.profile = const MedicalProfile(),
  });

  final MedicalProfilePhase phase;
  final MedicalProfile profile;

  MedicalProfileState copyWith({
    MedicalProfilePhase? phase,
    MedicalProfile? profile,
  }) {
    return MedicalProfileState(
      phase: phase ?? this.phase,
      profile: profile ?? this.profile,
    );
  }
}
