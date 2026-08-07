import '../../../core/storage/key_value_store.dart';
import '../domain/model/emergency_category.dart';
import '../domain/repository/emergency_taxonomy_repository.dart';
import 'emergency_taxonomy_cache_codec.dart';

/// Levée quand la source distante renvoie une taxonomie VIDE. Le référentiel
/// des urgences est statique et jamais vide : une réponse vide traduit un
/// déploiement incomplet ou une anomalie, pas un état valide. La traiter comme
/// un échec (repli cache, sinon erreur affichée) évite un écran d'urgence blanc.
class EmptyTaxonomyException implements Exception {
  const EmptyTaxonomyException();

  @override
  String toString() => 'EmptyTaxonomyException: la taxonomie distante est vide';
}

/// Décorateur hors ligne de la taxonomie des urgences.
///
/// Stratégie réseau d'abord : chaque succès met le cache local à jour ; en panne
/// réseau, la dernière taxonomie connue est servie (le référentiel est statique,
/// donc équivalente au réseau — aucun bandeau « hors ligne » nécessaire).
///
/// Une réponse distante VIDE est traitée comme un échec (jamais valide pour un
/// référentiel statique non vide) : repli sur le cache s'il existe, sinon
/// [EmptyTaxonomyException] remonte et l'écran propose un réessai — au lieu d'un
/// écran d'urgence blanc. Au tout premier lancement SANS réseau ni cache,
/// l'erreur remonte de même. L'appel des secours (SAMU/Pompiers) reste, lui,
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
      if (categories.isEmpty) {
        // Réponse vide = jamais valide : on ne l'affiche pas et on ne l'écrit
        // pas en cache (ne jamais écraser une bonne taxonomie). Repli cache,
        // sinon erreur.
        return _cachedOrThrow(const EmptyTaxonomyException());
      }
      await _store.write(cacheKey, EmergencyTaxonomyCacheCodec.encode(categories));
      return categories;
    } on Exception catch (error) {
      return _cachedOrThrow(error);
    }
  }

  /// Sert la dernière taxonomie connue en cache ; à défaut, propage [error].
  Future<List<EmergencyCategory>> _cachedOrThrow(Exception error) async {
    final cached = EmergencyTaxonomyCacheCodec.decode(await _store.read(cacheKey));
    if (cached == null) {
      throw error;
    }
    return cached;
  }
}
