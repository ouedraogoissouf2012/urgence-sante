import '../../../core/storage/key_value_store.dart';
import '../domain/model/cached.dart';
import '../domain/model/medical_need.dart';
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
class CachedOrientationRepository implements OrientationRepository {
  CachedOrientationRepository(
    this._api,
    this._store, {
    DateTime Function()? now,
  }) : _now = now ?? DateTime.now;

  static const String needsKey = 'offline_needs_v1';
  static const String centersKey = 'offline_centers_v1';

  final OrientationRemote _api;
  final KeyValueStore _store;
  final DateTime Function() _now;

  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async {
    try {
      final needs = await _api.loadNeeds();
      await _store.write(needsKey, OrientationCacheCodec.encodeNeeds(needs, _now()));
      return Cached.live(needs);
    } on Exception {
      final cached = OrientationCacheCodec.decodeNeeds(await _store.read(needsKey));
      if (cached == null) {
        rethrow;
      }
      return Cached.fromStore(cached.items, syncedAt: cached.syncedAt);
    }
  }

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  }) async {
    final results = await _api.recommend(
      latitude: latitude,
      longitude: longitude,
      serviceCode: serviceCode,
    );
    if (results.isNotEmpty) {
      await _store.write(
          centersKey, OrientationCacheCodec.encodeCenters(results, _now()));
    }
    return results;
  }

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters() async {
    final cached = OrientationCacheCodec.decodeCenters(await _store.read(centersKey));
    if (cached == null) {
      return null;
    }
    return Cached.fromStore(cached.items, syncedAt: cached.syncedAt);
  }
}
