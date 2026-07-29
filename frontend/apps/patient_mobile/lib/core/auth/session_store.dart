/// Contrat de stockage SÉCURISÉ de la session patient (jeton porteur).
///
/// Un jeton est un identifiant : il ne va PAS dans `shared_preferences` mais
/// dans un stockage chiffré (Keystore Android / Keychain iOS). Substituable par
/// un faux en mémoire pour les tests.
abstract interface class SessionStore {
  /// Jeton de session enregistré, ou `null` si aucun (non connecté).
  Future<String?> readToken();

  /// Enregistre le jeton après une inscription ou une connexion réussie.
  Future<void> saveToken(String token);

  /// Efface le jeton (déconnexion).
  Future<void> clear();
}
