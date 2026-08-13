/// Contrat de stockage d'un jeton opaque (session, accès API, …).
/// Substituable par un faux en test.
abstract interface class TokenStore {
  Future<String?> readToken();
  Future<void> saveToken(String token);
  Future<void> clear();
}
