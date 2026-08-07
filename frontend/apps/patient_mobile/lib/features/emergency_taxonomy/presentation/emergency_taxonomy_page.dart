import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../../medical_profile/presentation/emergency_card_page.dart';
import '../../orientation/presentation/orientation_results_page.dart';
import '../../orientation/presentation/widgets/emergency_call_bar.dart';
import '../domain/model/emergency_category.dart';
import 'emergency_taxonomy_state.dart';
import 'emergency_taxonomy_view_model.dart';

/// Écran d'accueil du parcours : sélection de l'urgence à DEUX niveaux
/// (grande catégorie → symptôme), fidèle au document client.
///
/// Les catégories « appel direct » (accidents, intoxications) affichent un
/// message d'appel des secours au lieu de lancer une recherche. La barre d'appel
/// d'urgence (SAMU/Pompiers) reste présente en permanence.
class EmergencyTaxonomyPage extends ConsumerWidget {
  const EmergencyTaxonomyPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final EmergencyTaxonomyState state =
        ref.watch(emergencyTaxonomyViewModelProvider);
    final EmergencyTaxonomyViewModel viewModel =
        ref.read(emergencyTaxonomyViewModelProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        leading: state.isLevelTwo
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                tooltip: 'Retour aux catégories',
                onPressed: viewModel.back,
              )
            : null,
        title: Text(state.selectedCategory?.label ?? 'Quelle est votre urgence ?'),
        actions: [
          IconButton(
            tooltip: 'Ma fiche d\'urgence',
            icon: const Icon(Icons.medical_information_outlined),
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(builder: (_) => const EmergencyCardPage()),
            ),
          ),
        ],
      ),
      body: _body(context, ref, state, viewModel),
      bottomNavigationBar: const EmergencyCallBar(),
    );
  }

  Widget _body(BuildContext context, WidgetRef ref, EmergencyTaxonomyState state,
      EmergencyTaxonomyViewModel viewModel) {
    return switch (state.phase) {
      EmergencyTaxonomyPhase.loading =>
        const AsyncStateView.loading(message: 'Chargement des urgences…'),
      EmergencyTaxonomyPhase.error => Center(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.lg),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.error_outline, size: AppSizing.iconLarge),
                const SizedBox(height: AppSpacing.md),
                Text(
                  state.errorMessage ?? 'Une erreur est survenue.',
                  style: AppTypography.body,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: AppSpacing.lg),
                FilledButton(
                  onPressed: viewModel.retry,
                  child: const Text('Réessayer'),
                ),
              ],
            ),
          ),
        ),
      EmergencyTaxonomyPhase.ready => state.selectedCategory == null
          ? _CategoryList(
              categories: state.categories,
              onSelect: viewModel.openCategory,
            )
          : _SymptomList(
              category: state.selectedCategory!,
              onSymptom: () => _onSymptomChosen(context, ref, state.selectedCategory!),
            ),
    };
  }

  /// Sur choix d'un symptôme : appel direct (catégories 4 & 7) OU recherche
  /// multi-services sur les services de la catégorie.
  void _onSymptomChosen(
      BuildContext context, WidgetRef ref, EmergencyCategory category) {
    if (category.directCallOnly) {
      _showDirectCall(context, ref, category);
      return;
    }
    Navigator.of(context).push(MaterialPageRoute<void>(
      builder: (_) => OrientationResultsPage(
        serviceCodes: category.serviceCodes,
        title: category.label,
      ),
    ));
  }

  void _showDirectCall(
      BuildContext context, WidgetRef ref, EmergencyCategory category) {
    final caller = ref.read(emergencyCallerProvider);
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Icon(Icons.warning_amber_rounded, size: AppSizing.iconLarge),
              const SizedBox(height: AppSpacing.md),
              Text(
                category.directCallMessage ??
                    'Appelez immédiatement les secours.',
                style: AppTypography.body,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: AppSpacing.lg),
              Row(
                children: [
                  Expanded(
                    child: EmergencyCallButton(
                      label: 'SAMU',
                      phoneNumber: '185',
                      onPressed: () => caller.call('185'),
                    ),
                  ),
                  const SizedBox(width: AppSpacing.sm),
                  Expanded(
                    child: EmergencyCallButton(
                      label: 'Pompiers',
                      phoneNumber: '180',
                      onPressed: () => caller.call('180'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Niveau 1 : les grandes catégories d'urgence.
class _CategoryList extends StatelessWidget {
  const _CategoryList({required this.categories, required this.onSelect});

  final List<EmergencyCategory> categories;
  final ValueChanged<EmergencyCategory> onSelect;

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: const EdgeInsets.all(AppSpacing.md),
      itemCount: categories.length,
      separatorBuilder: (_, _) => const SizedBox(height: AppSpacing.sm),
      itemBuilder: (context, index) {
        final EmergencyCategory category = categories[index];
        return Card(
          child: ListTile(
            title: Text(category.label, style: AppTypography.title),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => onSelect(category),
          ),
        );
      },
    );
  }
}

/// Niveau 2 : les symptômes de la catégorie choisie.
class _SymptomList extends StatelessWidget {
  const _SymptomList({required this.category, required this.onSymptom});

  final EmergencyCategory category;
  final VoidCallback onSymptom;

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: const EdgeInsets.all(AppSpacing.md),
      itemCount: category.symptoms.length,
      separatorBuilder: (_, _) => const SizedBox(height: AppSpacing.xs),
      itemBuilder: (context, index) {
        final Symptom symptom = category.symptoms[index];
        return Card(
          child: ListTile(
            title: Text(symptom.label, style: AppTypography.body),
            trailing: const Icon(Icons.chevron_right),
            onTap: onSymptom,
          ),
        );
      },
    );
  }
}
