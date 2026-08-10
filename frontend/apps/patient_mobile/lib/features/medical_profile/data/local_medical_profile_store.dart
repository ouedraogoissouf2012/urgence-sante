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
class LocalMedicalProfileStore implements ProfileStore {
  LocalMedicalProfileStore(this._secureStore, this._legacyStore);

  static const String _key = 'medical_profile_v1';

  final KeyValueStore _secureStore;
  final KeyValueStore _legacyStore;

  @override
  Future<MedicalProfile> load() async {
    final String? encrypted = await _secureStore.read(_key);
    if (encrypted != null && encrypted.isNotEmpty) {
      return MedicalProfileCodec.decode(encrypted);
    }
    return _migrateFromLegacyStore();
  }

  Future<MedicalProfile> _migrateFromLegacyStore() async {
    final String? legacy = await _legacyStore.read(_key);
    if (legacy == null || legacy.isEmpty) {
      return const MedicalProfile();
    }
    await _secureStore.write(_key, legacy);
    await _legacyStore.write(_key, '');
    return MedicalProfileCodec.decode(legacy);
  }

  @override
  Future<void> save(MedicalProfile profile) {
    return _secureStore.write(_key, MedicalProfileCodec.encode(profile));
  }

  @override
  Future<void> clear() => _secureStore.write(_key, '');
}
