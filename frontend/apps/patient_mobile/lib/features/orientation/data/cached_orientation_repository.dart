import '../../../core/storage/key_value_store.dart';
import '../domain/model/cached.dart';
import '../domain/model/recommended_center.dart';
import '../domain/repository/orientation_repository.dart';
import 'orientation_cache_codec.dart';
import 'orientation_remote.dart';

/// Décorateur hors ligne du parcours d'orientation (Lot 1).
///
/// Stratégie réseau d'abord : chaque succès réseau met le cache à jour ; en
/// panne réseau, le catalogue est servi depuis le cache (avec sa date), et les
/// derniers centres connus restent consultables via [lastKnownCenters] — leurs
/// statuts sont volontairement « UNKNOWN » (jamais présentés comme temps réel).
///
/// Le cache des centres est CLOISONNÉ PAR BESOIN (ensemble de services) : deux
/// urgences différentes ne partagent jamais leur repli hors ligne. Sans cela,
/// une recherche pour une urgence servirait, hors ligne, les centres mis en
/// cache pour une AUTRE urgence — orientation trompeuse (issue #107).
class CachedOrientationRepository implements OrientationRepository {
  CachedOrientationRepository(
    this._api,
    this._store, {
    DateTime Function()? now,
  }) : _now = now ?? DateTime.now;

  static const String centersKeyPrefix = 'offline_centers_v1';

  /// Clé de cache des centres pour un besoin donné. Les codes sont normalisés
  /// (minuscules, dédupliqués, triés) afin qu'un même besoin produise toujours
  /// la même clé, indépendamment de l'ordre des services.
  static String centersKeyFor(List<String> serviceCodes) {
    final normalized = serviceCodes
        .map((code) => code.toLowerCase().trim())
        .where((code) => code.isNotEmpty)
        .toSet()
        .toList()
      ..sort();
    return '$centersKeyPrefix:${normalized.join(',')}';
  }

  final OrientationRemote _api;
  final KeyValueStore _store;
  final DateTime Function() _now;

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async {
    final results = await _api.recommend(
      latitude: latitude,
      longitude: longitude,
      serviceCodes: serviceCodes,
    );
    if (results.isNotEmpty) {
      await _store.write(centersKeyFor(serviceCodes),
          OrientationCacheCodec.encodeCenters(results, _now()));
    }
    return results;
  }

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters({
    required List<String> serviceCodes,
  }) async {
    final cached = OrientationCacheCodec.decodeCenters(
        await _store.read(centersKeyFor(serviceCodes)));
    if (cached == null) {
      return null;
    }
    return Cached.fromStore(cached.items, syncedAt: cached.syncedAt);
  }
}
