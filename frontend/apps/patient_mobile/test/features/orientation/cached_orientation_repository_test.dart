import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/storage/key_value_store.dart';
import 'package:patient_mobile/features/orientation/data/cached_orientation_repository.dart';
import 'package:patient_mobile/features/orientation/data/orientation_remote.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
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
  Future<List<MedicalNeed>> loadNeeds() async {
    if (offline) throw Exception('réseau');
    return const [MedicalNeed(code: 'maternity', label: 'Maternité')];
  }

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
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

  test('cache vide + panne réseau → l\'erreur remonte', () async {
    remote.offline = true;

    expect(repository.loadNeeds, throwsException);
    expect(await repository.lastKnownCenters(), isNull);
  });

  test('un succès réseau remplit le cache ; la panne sert le cache daté', () async {
    await repository.loadNeeds();
    remote.offline = true;

    final cached = await repository.loadNeeds();

    expect(cached.fromCache, isTrue);
    expect(cached.syncedAt, syncTime);
    expect(cached.value.single.label, 'Maternité');
  });

  test('les derniers centres connus sont restitués SANS statut temps réel', () async {
    await repository.recommend(latitude: 5.35, longitude: -4.0, serviceCode: 'maternity');
    remote.offline = true;

    final cached = await repository.lastKnownCenters();

    expect(cached, isNotNull);
    expect(cached!.fromCache, isTrue);
    expect(cached.value.single.name, 'CHU de Cocody');
    // Jamais présenté comme temps réel : statut neutralisé, pas de trajet.
    expect(cached.value.single.status, 'UNKNOWN');
    expect(cached.value.single.travelTimeSeconds, isNull);
    expect(cached.value.single.phone, '+2250100000001');
  });

  test('des données corrompues sont traitées comme cache absent', () async {
    store.data[CachedOrientationRepository.needsKey] = '{pas du json';
    store.data[CachedOrientationRepository.centersKey] = '42';
    remote.offline = true;

    expect(repository.loadNeeds, throwsException);
    expect(await repository.lastKnownCenters(), isNull);
  });

  test('la péremption est calculable depuis la date de synchronisation', () async {
    await repository.loadNeeds();
    remote.offline = true;
    final cached = await repository.loadNeeds();

    expect(
      cached.isStale(const Duration(hours: 24), syncTime.add(const Duration(hours: 2))),
      isFalse,
    );
    expect(
      cached.isStale(const Duration(hours: 24), syncTime.add(const Duration(days: 2))),
      isTrue,
    );
  });
}
