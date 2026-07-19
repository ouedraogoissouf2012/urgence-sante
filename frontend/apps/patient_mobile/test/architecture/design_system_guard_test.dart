import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

/// Garde d'architecture (issue #47) : les widgets de l'application n'utilisent
/// que les jetons du design system, jamais de valeurs brutes (couleurs codées
/// en dur, marges numériques). Le design system est la seule source des styles.
void main() {
  test('aucune valeur de style brute hors du design system', () {
    final libDir = Directory('lib');
    final offenders = <String>[];

    // « Colors. » brut mais pas « AppColors. » (jeton du design system).
    final rawColor = RegExp(r'(?<![A-Za-z])Colors\.|Color\(0x');
    // Marges/tailles numériques littérales (autoriser 0 et .0 neutres).
    final rawInset = RegExp(r'EdgeInsets\.(all|symmetric|only)\(\s*[0-9]');

    for (final entity in libDir.listSync(recursive: true)) {
      if (entity is! File || !entity.path.endsWith('.dart')) continue;
      final lines = entity.readAsLinesSync();
      for (var i = 0; i < lines.length; i++) {
        final line = lines[i];
        if (line.trimLeft().startsWith('//')) continue;
        if (rawColor.hasMatch(line) || rawInset.hasMatch(line)) {
          offenders.add('${entity.path}:${i + 1}: ${line.trim()}');
        }
      }
    }

    expect(
      offenders,
      isEmpty,
      reason: 'Utilisez AppColors/AppSpacing/AppRadius :\n${offenders.join('\n')}',
    );
  });
}
