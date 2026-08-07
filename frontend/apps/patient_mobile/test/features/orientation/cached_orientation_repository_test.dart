import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/storage/key_value_store.dart';
import 'package:patient_mobile/features/orientation/data/cached_orientation_repository.dart';
import 'package:patient_mobile/features/orientation/data/orientation_remote.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';

class _MemoryStore implements KeyValueStore {
  final Map<String, String> data = {};

  @override
  Future<String?> read(String key) async => data[key];

  @override
  Future<void> write(String key, String value) async => data[key] = value;
}

class _FakeRemote implements OrientationRemote {
  bool offline = false;

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async {
    if (offline) throw Exception('réseau');
    return const [
      RecommendedCenter(
        facilityId: 'id-1',
        name: 'CHU de Cocody',
        latitude: 5.3496,
        longitude: -3.9851,
        phone: '+2250100000001',
        distanceMeters: 2800,
        travelTimeSeconds: 320,
        status: 'AVAILABLE',
        explanation: 'service disponible',
      ),
    ];
  }
}

void main() {
  final syncTime = DateTime.utc(2026, 7, 17, 10);
  late _MemoryStore store;
  late _FakeRemote remote;
  late CachedOrientationRepository repository;

  setUp(() {
    store = _MemoryStore();
    remote = _FakeRemote();
    repository = CachedOrientationRepository(remote, store, now: () => syncTime);
  });

  Future<List<RecommendedCenter>> search() => repository.recommend(
      latitude: 5.35, longitude: -4.0, serviceCodes: ['maternity']);

  test('cache vide + panne réseau : la recherche échoue, aucun centre connu', () async {
    remote.offline = true;

    expect(search, throwsException);
    expect(await repository.lastKnownCenters(serviceCodes: const ['maternity']), isNull);
  });

  test('un succès réseau remplit le cache des centres ; la panne le sert daté',
      () async {
    await search(); // succès réseau → cache mis à jour
    remote.offline = true;

    final cached = await repository.lastKnownCenters(serviceCodes: const ['maternity']);

    expect(cached, isNotNull);
    expect(cached!.fromCache, isTrue);
    expect(cached.syncedAt, syncTime);
  });

  test('les derniers centres connus sont restitués SANS statut temps réel', () async {
    await search();
    remote.offline = true;

    final cached = await repository.lastKnownCenters(serviceCodes: const ['maternity']);

    expect(cached, isNotNull);
    expect(cached!.fromCache, isTrue);
    expect(cached.value.single.name, 'CHU de Cocody');
    // Jamais présenté comme temps réel : statut neutralisé, pas de trajet.
    expect(cached.value.single.status, 'UNKNOWN');
    expect(cached.value.single.travelTimeSeconds, isNull);
    expect(cached.value.single.phone, '+2250100000001');
  });

  test('des données corrompues sont traitées comme cache absent', () async {
    store.data[CachedOrientationRepository.centersKeyFor(const ['maternity'])] =
        '{pas du json';

    expect(await repository.lastKnownCenters(serviceCodes: const ['maternity']), isNull);
  });

  test(
      'le repli hors ligne est CLOISONNÉ par besoin : un autre besoin ne sert '
      'jamais les centres mis en cache', () async {
    await search(); // succès réseau → cache rempli pour le besoin ['maternity']
    remote.offline = true;

    // Même besoin → repli disponible.
    expect(await repository.lastKnownCenters(serviceCodes: const ['maternity']),
        isNotNull);
    // Besoin DIFFÉRENT → aucun repli : on ne présente jamais les centres d'une
    // autre urgence comme réponse au besoin courant (issue #107).
    expect(await repository.lastKnownCenters(serviceCodes: const ['ophthalmology']),
        isNull);
  });

  test('l\'ordre des services ne change pas la clé de cache', () async {
    await repository.recommend(
        latitude: 5.35, longitude: -4.0, serviceCodes: ['neurology', 'intensive_care']);
    remote.offline = true;

    // Le même besoin exprimé dans un ordre différent retrouve le repli.
    expect(
        await repository
            .lastKnownCenters(serviceCodes: const ['intensive_care', 'neurology']),
        isNotNull);
  });

  test('la péremption est calculable depuis la date de synchronisation', () async {
    await search();
    remote.offline = true;
    final cached = await repository.lastKnownCenters(serviceCodes: const ['maternity']);

    expect(cached, isNotNull);
    expect(
      cached!.isStale(const Duration(hours: 24), syncTime.add(const Duration(hours: 2))),
      isFalse,
    );
    expect(
      cached.isStale(const Duration(hours: 24), syncTime.add(const Duration(days: 2))),
      isTrue,
    );
  });
}
