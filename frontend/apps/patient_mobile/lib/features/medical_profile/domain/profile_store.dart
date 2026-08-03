import 'medical_profile.dart';

/// Contrat de persistance LOCALE de la fiche médicale. Substituable en test.
///
/// La fiche ne quitte jamais l'appareil (aucune donnée de santé côté serveur).
/// L'implémentation ne plante jamais sur des données corrompues (fiche vide).
abstract interface class ProfileStore {
  /// Fiche enregistrée, ou une fiche vide si aucune / illisible.
  Future<MedicalProfile> load();

  /// Enregistre la fiche (remplace la précédente).
  Future<void> save(MedicalProfile profile);

  /// Supprime la fiche de l'appareil.
  Future<void> clear();
}
