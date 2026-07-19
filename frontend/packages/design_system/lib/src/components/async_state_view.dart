import 'package:flutter/material.dart';

import '../tokens/app_sizing.dart';
import '../tokens/app_spacing.dart';
import '../tokens/app_typography.dart';

/// Vues d'états asynchrones partagées : chargement, vide, erreur.
///
/// Utilisées par les applications pour des états soignés et cohérents
/// (critères des parcours patient et portail).
class AsyncStateView extends StatelessWidget {
  const AsyncStateView.loading({super.key, this.message})
      : _kind = _Kind.loading,
        onRetry = null;

  const AsyncStateView.empty({required String this.message, super.key})
      : _kind = _Kind.empty,
        onRetry = null;

  const AsyncStateView.error({required String this.message, this.onRetry, super.key})
      : _kind = _Kind.error;

  final _Kind _kind;
  final String? message;
  final VoidCallback? onRetry;

  @override
  Widget build(BuildContext context) {
    final List<Widget> children = switch (_kind) {
      _Kind.loading => [
          const CircularProgressIndicator(),
          if (message != null) ...[
            const SizedBox(height: AppSpacing.md),
            Text(message!, style: AppTypography.body, textAlign: TextAlign.center),
          ],
        ],
      _Kind.empty => [
          const Icon(Icons.search_off, size: AppSizing.iconLarge),
          const SizedBox(height: AppSpacing.md),
          Text(message!, style: AppTypography.body, textAlign: TextAlign.center),
        ],
      _Kind.error => [
          const Icon(Icons.error_outline, size: AppSizing.iconLarge),
          const SizedBox(height: AppSpacing.md),
          Text(message!, style: AppTypography.body, textAlign: TextAlign.center),
          if (onRetry != null) ...[
            const SizedBox(height: AppSpacing.lg),
            FilledButton(onPressed: onRetry, child: const Text('Réessayer')),
          ],
        ],
    };

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(mainAxisSize: MainAxisSize.min, children: children),
      ),
    );
  }
}

enum _Kind { loading, empty, error }
