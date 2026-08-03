import '../../../core/storage/key_value_store.dart';
import '../domain/medical_profile.dart';
import '../domain/profile_store.dart';
import 'medical_profile_codec.dart';

/// Persistance locale de la fiche médicale sur l'appareil (KeyValueStore →
/// shared_preferences). Clé versionnée : une évolution du format n'écrase pas
/// silencieusement des données incompatibles.
class LocalMedicalProfileStore implements ProfileStore {
  LocalMedicalProfileStore(this._store);

  static const String _key = 'medical_profile_v1';

  final KeyValueStore _store;

  @override
  Future<MedicalProfile> load() async {
    return MedicalProfileCodec.decode(await _store.read(_key));
  }

  @override
  Future<void> save(MedicalProfile profile) {
    return _store.write(_key, MedicalProfileCodec.encode(profile));
  }

  @override
  Future<void> clear() => _store.write(_key, '');
}
