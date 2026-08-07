import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/model/emergency_category.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/repository/emergency_taxonomy_repository.dart';
import 'package:patient_mobile/features/emergency_taxonomy/presentation/emergency_taxonomy_state.dart';
import 'package:patient_mobile/features/emergency_taxonomy/presentation/emergency_taxonomy_view_model.dart';

class _FakeTaxonomyRepository implements EmergencyTaxonomyRepository {
  _FakeTaxonomyRepository(this._categories, {this.fail = false});

  final List<EmergencyCategory> _categories;
  final bool fail;

  @override
  Future<List<EmergencyCategory>> fetchTaxonomy() async {
    if (fail) throw Exception('réseau');
    return _categories;
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
  EmergencyCategory(
    id: 'accidents',
    label: 'Accidents et traumatologie',
    directCallOnly: true,
    directCallMessage: 'Appelez les secours.',
    symptoms: [Symptom(id: 'fracture', label: 'Fracture')],
    serviceCodes: ['ortho_trauma'],
  ),
];

void main() {
  ProviderContainer containerWith(EmergencyTaxonomyRepository repository) {
    final container = ProviderContainer(overrides: [
      emergencyTaxonomyRepositoryProvider.overrideWithValue(repository),
    ]);
    addTearDown(container.dispose);
    return container;
  }

  Future<void> flush() => Future<void>.delayed(Duration.zero);

  test('charge la taxonomie au démarrage', () async {
    final container = containerWith(_FakeTaxonomyRepository(_categories));
    container.read(emergencyTaxonomyViewModelProvider.notifier);
    await flush();

    final state = container.read(emergencyTaxonomyViewModelProvider);
    expect(state.phase, EmergencyTaxonomyPhase.ready);
    expect(state.categories, hasLength(2));
    expect(state.isLevelTwo, isFalse);
  });

  test('ouvrir une catégorie passe au niveau 2, puis retour au niveau 1', () async {
    final container = containerWith(_FakeTaxonomyRepository(_categories));
    final vm = container.read(emergencyTaxonomyViewModelProvider.notifier);
    await flush();

    vm.openCategory(_categories.first);
    expect(container.read(emergencyTaxonomyViewModelProvider).selectedCategory!.id,
        'respiratoires');
    expect(container.read(emergencyTaxonomyViewModelProvider).isLevelTwo, isTrue);

    vm.back();
    expect(container.read(emergencyTaxonomyViewModelProvider).isLevelTwo, isFalse);
  });

  test('une panne réseau donne un état erreur', () async {
    final container = containerWith(_FakeTaxonomyRepository(const [], fail: true));
    container.read(emergencyTaxonomyViewModelProvider.notifier);
    await flush();

    expect(container.read(emergencyTaxonomyViewModelProvider).phase,
        EmergencyTaxonomyPhase.error);
  });
}
