import 'dart:convert';

import '../domain/model/emergency_category.dart';

/// Sérialisation JSON du cache hors ligne de la taxonomie des urgences.
///
/// Format versionné : {"v": 1, "categories": [...]}. Toute erreur de décodage
/// (ou version inconnue) est traitée comme cache absent — jamais de crash sur
/// données corrompues. La taxonomie étant un référentiel statique, aucune date
/// de synchronisation n'est nécessaire (la version en cache équivaut au réseau).
abstract final class EmergencyTaxonomyCacheCodec {
  static const int _version = 1;

  static String encode(List<EmergencyCategory> categories) => jsonEncode({
        'v': _version,
        'categories': [
          for (final category in categories)
            {
              'id': category.id,
              'label': category.label,
              'directCallOnly': category.directCallOnly,
              'directCallMessage': category.directCallMessage,
              'symptoms': [
                for (final symptom in category.symptoms)
                  {'id': symptom.id, 'label': symptom.label},
              ],
              'serviceCodes': category.serviceCodes,
            },
        ],
      });

  static List<EmergencyCategory>? decode(String? raw) {
    if (raw == null || raw.isEmpty) {
      return null;
    }
    try {
      final map = jsonDecode(raw) as Map<String, Object?>;
      if (map['v'] != _version) {
        return null;
      }
      final categories =
          (map['categories'] as List<Object?>).cast<Map<String, Object?>>();
      return [
        for (final category in categories)
          EmergencyCategory(
            id: category['id'] as String,
            label: category['label'] as String,
            directCallOnly: category['directCallOnly'] as bool,
            directCallMessage: category['directCallMessage'] as String?,
            symptoms: [
              for (final symptom in (category['symptoms'] as List<Object?>)
                  .cast<Map<String, Object?>>())
                Symptom(id: symptom['id'] as String, label: symptom['label'] as String),
            ],
            serviceCodes: (category['serviceCodes'] as List<Object?>).cast<String>(),
          ),
      ];
    } on Object {
      return null;
    }
  }
}
