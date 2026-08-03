import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/medical_profile.dart';
import 'medical_profile_state.dart';
import 'medical_profile_view_model.dart';

/// Formulaire de saisie de la fiche médicale d'urgence. Tous les champs sont
/// facultatifs. Les données restent sur l'appareil (jamais transmises).
class MedicalProfileFormPage extends ConsumerStatefulWidget {
  const MedicalProfileFormPage({super.key});

  @override
  ConsumerState<MedicalProfileFormPage> createState() =>
      _MedicalProfileFormPageState();
}

class _MedicalProfileFormPageState
    extends ConsumerState<MedicalProfileFormPage> {
  final _fullName = TextEditingController();
  final _allergies = TextEditingController();
  final _conditions = TextEditingController();
  final _treatments = TextEditingController();
  final _contactName = TextEditingController();
  final _contactPhone = TextEditingController();
  final _notes = TextEditingController();
  BloodType? _bloodType;
  bool _initialised = false;

  @override
  void dispose() {
    for (final c in [
      _fullName, _allergies, _conditions, _treatments,
      _contactName, _contactPhone, _notes,
    ]) {
      c.dispose();
    }
    super.dispose();
  }

  /// Pré-remplit les champs depuis la fiche chargée, une seule fois.
  void _hydrate(MedicalProfile p) {
    if (_initialised) return;
    _initialised = true;
    _fullName.text = p.fullName ?? '';
    _allergies.text = p.allergies ?? '';
    _conditions.text = p.conditions ?? '';
    _treatments.text = p.treatments ?? '';
    _contactName.text = p.emergencyContactName ?? '';
    _contactPhone.text = p.emergencyContactPhone ?? '';
    _notes.text = p.notes ?? '';
    _bloodType = p.bloodType;
  }

  String? _trimmedOrNull(TextEditingController c) {
    final text = c.text.trim();
    return text.isEmpty ? null : text;
  }

  Future<void> _save() async {
    await ref.read(medicalProfileViewModelProvider.notifier).save(MedicalProfile(
          fullName: _trimmedOrNull(_fullName),
          bloodType: _bloodType,
          allergies: _trimmedOrNull(_allergies),
          conditions: _trimmedOrNull(_conditions),
          treatments: _trimmedOrNull(_treatments),
          emergencyContactName: _trimmedOrNull(_contactName),
          emergencyContactPhone: _trimmedOrNull(_contactPhone),
          notes: _trimmedOrNull(_notes),
        ));
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final MedicalProfileState state =
        ref.watch(medicalProfileViewModelProvider);
    if (state.phase == MedicalProfilePhase.loading) {
      return const Scaffold(body: AsyncStateView.loading());
    }
    _hydrate(state.profile);

    return Scaffold(
      appBar: AppBar(title: const Text('Ma fiche d\'urgence')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            const InfoCard(
              icon: Icons.lock_outline,
              title: 'Reste sur votre téléphone',
              body: 'Ces informations ne sont ni envoyées ni partagées. '
                  'Elles s\'affichent en grand pour les montrer aux secours.',
            ),
            const SizedBox(height: AppSpacing.lg),

            AppTextField(
              label: 'Nom complet',
              controller: _fullName,
              textInputAction: TextInputAction.next,
            ),
            const SizedBox(height: AppSpacing.md),

            const Text('Groupe sanguin', style: AppTypography.buttonLabel),
            const SizedBox(height: AppSpacing.xs),
            Wrap(
              spacing: AppSpacing.sm,
              runSpacing: AppSpacing.sm,
              children: [
                for (final type in BloodType.values)
                  ChoiceChip(
                    label: Text(type.label),
                    selected: _bloodType == type,
                    onSelected: (sel) =>
                        setState(() => _bloodType = sel ? type : null),
                  ),
              ],
            ),
            const SizedBox(height: AppSpacing.md),

            AppTextField(
              label: 'Allergies',
              hint: 'Ex. pénicilline, arachides…',
              controller: _allergies,
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              label: 'Pathologies / antécédents',
              hint: 'Ex. asthme, diabète…',
              controller: _conditions,
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              label: 'Traitements en cours',
              controller: _treatments,
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              label: 'Personne à prévenir',
              controller: _contactName,
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              label: 'Téléphone à prévenir',
              hint: '+225 XX XX XX XX XX',
              controller: _contactPhone,
              keyboardType: TextInputType.phone,
              inputFormatters: [
                FilteringTextInputFormatter.allow(RegExp(r'[0-9+\s().\-]')),
              ],
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              label: 'Note libre',
              controller: _notes,
            ),

            const SizedBox(height: AppSpacing.xl),
            FilledButton.icon(
              onPressed: _save,
              icon: const Icon(Icons.save_outlined),
              label: const Text('Enregistrer'),
            ),
          ],
        ),
      ),
    );
  }
}
