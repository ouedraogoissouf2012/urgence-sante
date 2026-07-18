/// Version courante des conditions d'utilisation. Toute évolution du texte
/// incrémente cette valeur : les utilisateurs devront ré-accepter.
const String currentTermsVersion = '2026-07-v1';

/// Contrat de persistance du consentement. Substituable par un faux en test.
abstract interface class ConsentStore {
  /// Version des conditions acceptée par l'utilisateur, ou `null`.
  Future<String?> acceptedTermsVersion();

  /// Enregistre l'acceptation de la version donnée.
  Future<void> acceptTerms(String version);
}

/// Vrai si la version acceptée correspond à la version courante : les
/// conditions ne sont PAS redemandées inutilement, mais le sont après une
/// évolution du texte.
bool isConsentUpToDate(String? acceptedVersion) =>
    acceptedVersion == currentTermsVersion;
