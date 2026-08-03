import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/medical_profile.dart';
import 'medical_profile_form_page.dart';
import 'medical_profile_state.dart';
import 'medical_profile_view_model.dart';

/// « Ma fiche d'urgence » : affichage GRAND FORMAT, lisible d'un coup d'œil,
/// à présenter aux secours. Utilisable hors ligne (données locales). Depuis ici
/// on peut modifier ou effacer la fiche.
class EmergencyCardPage extends ConsumerWidget {
  const EmergencyCardPage({super.key});

  Future<void> _edit(BuildContext context) {
    return Navigator.of(context).push(MaterialPageRoute<void>(
      builder: (_) => const MedicalProfileFormPage(),
    ));
  }

  Future<void> _confirmClear(BuildContext context, WidgetRef ref) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Effacer la fiche ?'),
        content: const Text(
            'Vos informations médicales seront supprimées de cet appareil.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: const Text('Annuler'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            child: const Text('Effacer'),
          ),
        ],
      ),
    );
    if (ok ?? false) {
      await ref.read(medicalProfileViewModelProvider.notifier).clear();
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final MedicalProfileState state =
        ref.watch(medicalProfileViewModelProvider);

    if (state.phase == MedicalProfilePhase.loading) {
      return const Scaffold(body: AsyncStateView.loading());
    }

    final MedicalProfile p = state.profile;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Ma fiche d\'urgence'),
        actions: [
          IconButton(
            tooltip: 'Modifier',
            icon: const Icon(Icons.edit_outlined),
            onPressed: () => _edit(context),
          ),
          if (p.isNotEmpty)
            IconButton(
              tooltip: 'Effacer',
              icon: const Icon(Icons.delete_outline),
              onPressed: () => _confirmClear(context, ref),
            ),
        ],
      ),
      body: SafeArea(
        child: p.isEmpty
            ? _EmptyState(onCreate: () => _edit(context))
            : _CardView(profile: p),
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({required this.onCreate});

  final VoidCallback onCreate;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.medical_information_outlined,
                size: AppSizing.iconLarge),
            const SizedBox(height: AppSpacing.md),
            const Text(
              'Créez votre fiche d\'urgence : groupe sanguin, allergies, '
              'traitements. Elle s\'affichera ici pour la montrer aux secours.',
              style: AppTypography.body,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.lg),
            FilledButton.icon(
              onPressed: onCreate,
              icon: const Icon(Icons.add),
              label: const Text('Créer ma fiche'),
            ),
          ],
        ),
      ),
    );
  }
}

class _CardView extends StatelessWidget {
  const _CardView({required this.profile});

  final MedicalProfile profile;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.lg),
      children: [
        if (profile.fullName != null)
          Text(profile.fullName!,
              style: AppTypography.headline, textAlign: TextAlign.center),
        if (profile.bloodType != null) ...[
          const SizedBox(height: AppSpacing.md),
          Center(
            child: Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.lg, vertical: AppSpacing.sm),
              decoration: BoxDecoration(
                color: scheme.errorContainer,
                borderRadius: AppRadius.button,
              ),
              child: Text(
                'Groupe sanguin  ${profile.bloodType!.label}',
                style: AppTypography.title
                    .copyWith(color: scheme.onErrorContainer),
              ),
            ),
          ),
        ],
        const SizedBox(height: AppSpacing.lg),
        _Field(label: 'Allergies', value: profile.allergies),
        _Field(label: 'Pathologies / antécédents', value: profile.conditions),
        _Field(label: 'Traitements en cours', value: profile.treatments),
        _Field(
          label: 'Personne à prévenir',
          value: _contact(profile),
        ),
        _Field(label: 'Note', value: profile.notes),
      ],
    );
  }

  String? _contact(MedicalProfile p) {
    final parts = [p.emergencyContactName, p.emergencyContactPhone]
        .where((v) => v != null && v.trim().isNotEmpty)
        .join(' · ');
    return parts.isEmpty ? null : parts;
  }
}

/// Un champ de la fiche, en grand, masqué s'il est vide.
class _Field extends StatelessWidget {
  const _Field({required this.label, required this.value});

  final String label;
  final String? value;

  @override
  Widget build(BuildContext context) {
    if (value == null || value!.trim().isEmpty) {
      return const SizedBox.shrink();
    }
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: AppTypography.caption),
          const SizedBox(height: AppSpacing.xs),
          Text(value!, style: AppTypography.title),
        ],
      ),
    );
  }
}
