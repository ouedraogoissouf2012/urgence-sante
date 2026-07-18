import 'dart:convert';

import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';

/// Sérialisation JSON du cache hors ligne (besoins et derniers centres).
///
/// Format : {"syncedAt": ISO-8601 UTC, "items": [...]}. Toute erreur de
/// décodage est traitée comme cache absent (jamais de crash sur données
/// corrompues).
abstract final class OrientationCacheCodec {
  static String encodeNeeds(List<MedicalNeed> needs, DateTime syncedAt) =>
      jsonEncode({
        'syncedAt': syncedAt.toUtc().toIso8601String(),
        'items': [
          for (final need in needs) {'code': need.code, 'label': need.label},
        ],
      });

  static ({List<MedicalNeed> items, DateTime syncedAt})? decodeNeeds(String? raw) {
    final decoded = _envelope(raw);
    if (decoded == null) {
      return null;
    }
    try {
      final items = [
        for (final item in decoded.items)
          MedicalNeed(
            code: item['code'] as String,
            label: item['label'] as String,
          ),
      ];
      return (items: items, syncedAt: decoded.syncedAt);
    } on Object {
      return null;
    }
  }

  static String encodeCenters(List<RecommendedCenter> centers, DateTime syncedAt) =>
      jsonEncode({
        'syncedAt': syncedAt.toUtc().toIso8601String(),
        'items': [
          for (final center in centers)
            {
              'facilityId': center.facilityId,
              'name': center.name,
              'latitude': center.latitude,
              'longitude': center.longitude,
              'phone': center.phone,
              'distanceMeters': center.distanceMeters,
            },
        ],
      });

  /// Les statuts et temps de trajet ne sont volontairement PAS restitués :
  /// une donnée hors ligne n'est jamais présentée comme du temps réel
  /// (statut « UNKNOWN », pas d'estimation de trajet).
  static ({List<RecommendedCenter> items, DateTime syncedAt})? decodeCenters(
      String? raw) {
    final decoded = _envelope(raw);
    if (decoded == null) {
      return null;
    }
    try {
      final items = [
        for (final item in decoded.items)
          RecommendedCenter(
            facilityId: item['facilityId'] as String,
            name: item['name'] as String,
            latitude: (item['latitude'] as num).toDouble(),
            longitude: (item['longitude'] as num).toDouble(),
            phone: item['phone'] as String?,
            distanceMeters: (item['distanceMeters'] as num).toDouble(),
            status: 'UNKNOWN',
            explanation: 'données locales, disponibilité non confirmée',
          ),
      ];
      return (items: items, syncedAt: decoded.syncedAt);
    } on Object {
      return null;
    }
  }

  static ({List<Map<String, Object?>> items, DateTime syncedAt})? _envelope(
      String? raw) {
    if (raw == null || raw.isEmpty) {
      return null;
    }
    try {
      final map = jsonDecode(raw) as Map<String, Object?>;
      final syncedAt = DateTime.parse(map['syncedAt'] as String);
      final items = (map['items'] as List<Object?>)
          .cast<Map<String, Object?>>()
          .toList();
      return (items: items, syncedAt: syncedAt);
    } on Object {
      return null;
    }
  }
}
