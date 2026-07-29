import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'session_store.dart';

/// Implémentation de {@link SessionStore} adossée au stockage chiffré du système
/// (Android Keystore / iOS Keychain) via `flutter_secure_storage`.
class SecureSessionStore implements SessionStore {
  SecureSessionStore([FlutterSecureStorage? storage])
      : _storage = storage ?? const FlutterSecureStorage();

  static const String _tokenKey = 'patient_session_token';

  final FlutterSecureStorage _storage;

  @override
  Future<String?> readToken() => _storage.read(key: _tokenKey);

  @override
  Future<void> saveToken(String token) => _storage.write(key: _tokenKey, value: token);

  @override
  Future<void> clear() => _storage.delete(key: _tokenKey);
}
