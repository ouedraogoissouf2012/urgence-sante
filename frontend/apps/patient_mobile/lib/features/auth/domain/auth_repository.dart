/// Contrat d'authentification patient. Substituable par un faux en test (LSP).
///
/// Les implémentations traduisent les erreurs HTTP du backend en
/// [AuthException] typées ; le view-model ne connaît jamais le client API.
abstract interface class AuthRepository {
  /// Inscrit un patient et renvoie le jeton de session.
  Future<String> register({required String phone, required String password});

  /// Connecte un patient et renvoie le jeton de session.
  Future<String> login({required String phone, required String password});
}
