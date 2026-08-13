import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'token_store.dart';

/// Implémentation de [TokenStore] adossée au stockage chiffré du système
/// (Android Keystore / iOS Keychain) via `flutter_secure_storage`.
///
/// La clé de stockage est fournie par l'appelant (pas de valeur figée) pour
/// que plusieurs applications puissent l'utiliser sans collision et sans
/// dupliquer cette classe.
class SecureTokenStore implements TokenStore {
  SecureTokenStore(this._storageKey, [FlutterSecureStorage? storage])
      : _storage = storage ?? const FlutterSecureStorage();

  final String _storageKey;
  final FlutterSecureStorage _storage;

  @override
  Future<String?> readToken() => _storage.read(key: _storageKey);

  @override
  Future<void> saveToken(String token) => _storage.write(key: _storageKey, value: token);

  @override
  Future<void> clear() => _storage.delete(key: _storageKey);
}
