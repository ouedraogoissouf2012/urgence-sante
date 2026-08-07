import '../../../core/storage/key_value_store.dart';
import '../domain/model/emergency_category.dart';
import '../domain/repository/emergency_taxonomy_repository.dart';
import 'emergency_taxonomy_cache_codec.dart';

/// Décorateur hors ligne de la taxonomie des urgences.
///
/// Stratégie réseau d'abord : chaque succès met le cache local à jour ; en panne
/// réseau, la dernière taxonomie connue est servie (le référentiel est statique,
/// donc équivalente au réseau — aucun bandeau « hors ligne » nécessaire).
///
/// Au tout premier lancement SANS réseau ni cache, l'erreur remonte (l'écran
/// affiche alors un réessai) — l'appel des secours (SAMU/Pompiers) reste, lui,
/// accessible en permanence via la barre d'urgence.
class CachedEmergencyTaxonomyRepository implements EmergencyTaxonomyRepository {
  CachedEmergencyTaxonomyRepository(this._remote, this._store);

  static const String cacheKey = 'offline_emergency_taxonomy_v1';

  final EmergencyTaxonomyRepository _remote;
  final KeyValueStore _store;

  @override
  Future<List<EmergencyCategory>> fetchTaxonomy() async {
    try {
      final categories = await _remote.fetchTaxonomy();
      // On ne met en cache qu'un résultat non vide : ne jamais écraser une bonne
      // taxonomie en cache par une réponse vide inattendue.
      if (categories.isNotEmpty) {
        await _store.write(cacheKey, EmergencyTaxonomyCacheCodec.encode(categories));
      }
      return categories;
    } on Exception {
      final cached = EmergencyTaxonomyCacheCodec.decode(await _store.read(cacheKey));
      if (cached == null) {
        rethrow;
      }
      return cached;
    }
  }
}
