import 'package:app_foundation/app_foundation.dart';

/// Faux stockage de jeton en mémoire, pour les tests de `hospital_portal`.
class FakeTokenStore implements TokenStore {
  FakeTokenStore([String? initialToken]) : _token = initialToken;

  String? _token;

  @override
  Future<String?> readToken() async => _token;

  @override
  Future<void> saveToken(String token) async => _token = token;

  @override
  Future<void> clear() async => _token = null;
}
