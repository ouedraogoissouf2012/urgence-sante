import 'package:flutter/widgets.dart';

import '../components/async_state_view.dart';
import '../components/emergency_call_button.dart';
import '../components/status_badge.dart';

/// Entrée du catalogue : un composant nommé avec un exemple constructible.
class CatalogEntry {
  const CatalogEntry({required this.name, required this.builder});

  final String name;
  final WidgetBuilder builder;
}

/// Catalogue des composants du design system.
///
/// Chaque composant y est représenté par un exemple constructible : le
/// catalogue est monté en widget test (aucune entrée ne peut casser
/// silencieusement) et sert de base à une future galerie visuelle.
abstract final class ComponentCatalog {
  static void _noop() {}

  static final List<CatalogEntry> entries = List.unmodifiable([
    CatalogEntry(
      name: 'EmergencyCallButton',
      builder: (_) => const EmergencyCallButton(
        label: 'SAMU',
        phoneNumber: '185',
        onPressed: _noop,
      ),
    ),
    CatalogEntry(
      name: 'StatusBadge — tous les statuts',
      builder: (_) => const Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          StatusBadge(kind: StatusBadgeKind.available),
          StatusBadge(kind: StatusBadgeKind.limited),
          StatusBadge(kind: StatusBadgeKind.saturated),
          StatusBadge(kind: StatusBadgeKind.closed),
          StatusBadge(kind: StatusBadgeKind.unknown),
        ],
      ),
    ),
    CatalogEntry(
      name: 'AsyncStateView — chargement',
      builder: (_) => const AsyncStateView.loading(message: 'Recherche des centres…'),
    ),
    CatalogEntry(
      name: 'AsyncStateView — vide',
      builder: (_) => const AsyncStateView.empty(message: 'Aucun centre trouvé.'),
    ),
    CatalogEntry(
      name: 'AsyncStateView — erreur',
      builder: (_) => const AsyncStateView.error(message: 'Connexion impossible.', onRetry: _noop),
    ),
  ]);
}
