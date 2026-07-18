import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../domain/model/service_line.dart';
import 'portal_state.dart';
import 'portal_view_model.dart';
import 'widgets/service_line_tile.dart';

/// Écran principal du portail : accès (démo) puis tableau de disponibilité.
class PortalPage extends ConsumerWidget {
  const PortalPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final PortalState state = ref.watch(portalViewModelProvider);
    final PortalViewModel viewModel = ref.read(portalViewModelProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: Text(state.selectedFacility?.name ?? 'Portail hospitalier'),
      ),
      body: switch (state.phase) {
        PortalPhase.loadingFacilities =>
          const AsyncStateView.loading(message: 'Chargement des établissements…'),
        PortalPhase.loadingBoard =>
          const AsyncStateView.loading(message: 'Chargement du tableau…'),
        PortalPhase.error => AsyncStateView.error(
            message: state.errorMessage ?? 'Une erreur est survenue.',
            onRetry: viewModel.retry,
          ),
        PortalPhase.selectFacility => _facilityPicker(state, viewModel),
        PortalPhase.board => _board(context, state, viewModel),
      },
    );
  }

  Widget _facilityPicker(PortalState state, PortalViewModel viewModel) {
    if (state.facilities.isEmpty) {
      return const AsyncStateView.empty(message: 'Aucun établissement disponible.');
    }
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        const Text('Accès agent — démonstration', style: AppTypography.title),
        const SizedBox(height: AppSpacing.xs),
        const Text(
          'Sélectionnez votre établissement (authentification à venir).',
          style: AppTypography.caption,
        ),
        const SizedBox(height: AppSpacing.md),
        for (final facility in state.facilities)
          Card(
            child: ListTile(
              title: Text(facility.name),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => viewModel.enter(facility),
            ),
          ),
      ],
    );
  }

  Widget _board(BuildContext context, PortalState state, PortalViewModel viewModel) {
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        const Text('Disponibilité des services', style: AppTypography.title),
        const SizedBox(height: AppSpacing.sm),
        for (final line in state.lines)
          ServiceLineTile(
            line: line,
            updating: state.updatingServiceCode == line.serviceCode,
            onStatusSelected: (status) => viewModel.setStatus(line, status),
            onHistoryRequested: () => _showHistory(context, viewModel, line),
          ),
      ],
    );
  }

  Future<void> _showHistory(
      BuildContext context, PortalViewModel viewModel, ServiceLine line) async {
    final List<HistoryEntry> entries = await viewModel.history(line);
    if (!context.mounted) {
      return;
    }
    final DateFormat format = DateFormat('dd/MM/yyyy HH:mm');
    await showModalBottomSheet<void>(
      context: context,
      builder: (_) => ListView(
        padding: const EdgeInsets.all(AppSpacing.md),
        children: [
          Text('Historique — ${line.label}', style: AppTypography.title),
          const SizedBox(height: AppSpacing.sm),
          if (entries.isEmpty)
            const Text('Aucune mise à jour enregistrée.', style: AppTypography.body),
          for (final entry in entries)
            ListTile(
              leading: StatusBadge.fromApi(entry.status),
              title: Text(format.format(entry.updatedAt.toLocal())),
            ),
        ],
      ),
    );
  }
}
