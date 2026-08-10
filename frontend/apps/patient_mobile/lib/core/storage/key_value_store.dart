import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Port de stockage local clé/valeur. Substituable (mémoire en test) sans
/// modifier les couches supérieures.
abstract interface class KeyValueStore {
  Future<String?> read(String key);

  Future<void> write(String key, String value);
}

/// Implémentation SharedPreferences (persistance sur l'appareil, EN CLAIR).
///
/// Réservée aux données non sensibles (caches hors ligne). Pour une donnée
/// sensible (santé, session), utiliser [SecureFlutterKeyValueStore].
class SharedPrefsKeyValueStore implements KeyValueStore {
  const SharedPrefsKeyValueStore();

  @override
  Future<String?> read(String key) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(key);
  }

  @override
  Future<void> write(String key, String value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(key, value);
  }
}

/// Implémentation adossée au stockage CHIFFRÉ du système (Android Keystore /
/// iOS Keychain) via `flutter_secure_storage`, pour toute donnée sensible
/// (santé, session).
class SecureFlutterKeyValueStore implements KeyValueStore {
  SecureFlutterKeyValueStore([FlutterSecureStorage? storage])
      : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  @override
  Future<String?> read(String key) => _storage.read(key: key);

  @override
  Future<void> write(String key, String value) =>
      _storage.write(key: key, value: value);
}
