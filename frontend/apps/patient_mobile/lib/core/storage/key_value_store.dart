import 'package:shared_preferences/shared_preferences.dart';

/// Port de stockage local clé/valeur. Substituable (mémoire en test) sans
/// modifier les couches supérieures.
abstract interface class KeyValueStore {
  Future<String?> read(String key);

  Future<void> write(String key, String value);
}

/// Implémentation SharedPreferences (persistance sur l'appareil).
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
