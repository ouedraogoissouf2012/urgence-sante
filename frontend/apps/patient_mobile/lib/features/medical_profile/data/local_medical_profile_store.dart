import 'package:meta/meta.dart';

import '../../../core/storage/key_value_store.dart';
import '../domain/medical_profile.dart';
import '../domain/profile_store.dart';
import 'medical_profile_codec.dart';

/// Persistance locale CHIFFRÉE de la fiche médicale sur l'appareil
/// ([_secureStore], adossé à `flutter_secure_storage` — Keystore/Keychain).
/// Clé versionnée : une évolution du format n'écrase pas silencieusement des
/// données incompatibles.
///
/// Migration (correctif #128) : les versions antérieures enregistraient la
/// fiche EN CLAIR dans [_legacyStore] (`shared_preferences`). À la première
/// lecture sans fiche chiffrée, une fiche en clair existante est reprise dans
/// le stockage chiffré puis effacée de l'ancien emplacement — elle n'y traîne
/// plus une fois la migration faite.
///
/// Migration de clé (issue #140) : [_keysNewestFirst] liste toutes les clés
/// qu'a portées la fiche chiffrée, de la plus récente à la plus ancienne. Si
/// une future évolution du format bascule la clé courante (ex. schéma v2
/// incompatible), AJOUTER la nouvelle clé EN TÊTE de cette liste — ne jamais
/// remplacer une clé existante. `load()` retrouve alors la fiche sous une clé
/// plus ancienne et la reprend automatiquement sous la clé courante, au lieu
/// de la perdre silencieusement.
class LocalMedicalProfileStore implements ProfileStore {
  /// [keysNewestFirst] n'est à fournir qu'en test (scénario "future
  /// migration") : l'application utilise toujours la liste réelle par défaut.
  LocalMedicalProfileStore(
    this._secureStore,
    this._legacyStore, {
    @visibleForTesting List<String> keysNewestFirst = _keysNewestFirst,
  }) : _keysNewestFirstInstance = keysNewestFirst;

  static const String _currentKey = 'medical_profile_v1';

  /// Clés chiffrées historiques, de la plus récente à la plus ancienne.
  static const List<String> _keysNewestFirst = [_currentKey];

  final KeyValueStore _secureStore;
  final KeyValueStore _legacyStore;
  final List<String> _keysNewestFirstInstance;

  @override
  Future<MedicalProfile> load() async {
    for (final String key in _keysNewestFirstInstance) {
      final String? encrypted = await _secureStore.read(key);
      if (encrypted == null || encrypted.isEmpty) {
        continue;
      }
      if (key != _currentKey) {
        await _secureStore.write(_currentKey, encrypted);
        // Ne traîne plus sous l'ancienne clé une fois reprise (même hygiène
        // que la migration depuis le stockage en clair, ci-dessus).
        await _secureStore.write(key, '');
      }
      return MedicalProfileCodec.decode(encrypted);
    }
    return _migrateFromLegacyStore();
  }

  Future<MedicalProfile> _migrateFromLegacyStore() async {
    final String? legacy = await _legacyStore.read(_currentKey);
    if (legacy == null || legacy.isEmpty) {
      return const MedicalProfile();
    }
    await _secureStore.write(_currentKey, legacy);
    await _legacyStore.write(_currentKey, '');
    return MedicalProfileCodec.decode(legacy);
  }

  @override
  Future<void> save(MedicalProfile profile) {
    return _secureStore.write(_currentKey, MedicalProfileCodec.encode(profile));
  }

  @override
  Future<void> clear() => _secureStore.write(_currentKey, '');
}
