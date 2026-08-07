import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/emergency_taxonomy/data/cached_emergency_taxonomy_repository.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/model/emergency_category.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/repository/emergency_taxonomy_repository.dart';

import '../../auth/support/auth_fakes.dart' show InMemoryKeyValueStore;

class _FakeRemote implements EmergencyTaxonomyRepository {
  _FakeRemote(this.categories);

  List<EmergencyCategory> categories;
  bool offline = false;

  @override
  Future<List<EmergencyCategory>> fetchTaxonomy() async {
    if (offline) throw Exception('réseau indisponible');
    return categories;
  }
}

const _categories = [
  EmergencyCategory(
    id: 'respiratoires',
    label: 'Urgences respiratoires',
    directCallOnly: false,
    symptoms: [Symptom(id: 'crise-asthme', label: "Crise d'asthme")],
    serviceCodes: ['pulmonology'],
  ),
];

void main() {
  late InMemoryKeyValueStore store;
  late _FakeRemote remote;
  late CachedEmergencyTaxonomyRepository repository;

  setUp(() {
    store = InMemoryKeyValueStore();
    remote = _FakeRemote(_categories);
    repository = CachedEmergencyTaxonomyRepository(remote, store);
  });

  test('un succès réseau alimente le cache local', () async {
    final result = await repository.fetchTaxonomy();
    expect(result, hasLength(1));

    // Cache rempli : un appel HORS LIGNE suivant sert la taxonomie mémorisée.
    remote.offline = true;
    final cached = await repository.fetchTaxonomy();
    expect(cached.single.id, 'respiratoires');
    expect(cached.single.symptoms.single.label, "Crise d'asthme");
  });

  test('hors ligne sans cache : l\'erreur remonte', () async {
    remote.offline = true;
    expect(repository.fetchTaxonomy, throwsException);
  });

  test('un résultat vide revient au cache valide plutôt que de renvoyer vide',
      () async {
    await repository.fetchTaxonomy(); // remplit le cache
    remote.categories = const []; // le réseau renvoie une liste vide (jamais valide)

    // Le cache valide prime sur une réponse vide : jamais d'écran d'urgence
    // blanc, et le cache n'est pas écrasé.
    final result = await repository.fetchTaxonomy();
    expect(result.single.id, 'respiratoires');
  });

  test('un résultat vide SANS cache lève une erreur (jamais de liste vide)',
      () async {
    remote.categories = const [];
    expect(repository.fetchTaxonomy, throwsA(isA<EmptyTaxonomyException>()));
  });
}
