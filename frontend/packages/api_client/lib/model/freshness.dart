//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

/// Fraîcheur de l'information, calculée depuis sa dernière mise à jour.
class Freshness {
  /// Instantiate a new enum with the provided [value].
  const Freshness._(this.value);

  /// The underlying value of this enum member.
  final String value;

  @override
  String toString() => value;

  String toJson() => value;

  static const FRESH = Freshness._(r'FRESH');
  static const AGING = Freshness._(r'AGING');
  static const STALE = Freshness._(r'STALE');

  /// List of all possible values in this [enum][Freshness].
  static const values = <Freshness>[
    FRESH,
    AGING,
    STALE,
  ];

  static Freshness? fromJson(dynamic value) => FreshnessTypeTransformer().decode(value);

  static List<Freshness> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Freshness>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Freshness.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }
}

/// Transformation class that can [encode] an instance of [Freshness] to String,
/// and [decode] dynamic data back to [Freshness].
class FreshnessTypeTransformer {
  factory FreshnessTypeTransformer() => _instance ??= const FreshnessTypeTransformer._();

  const FreshnessTypeTransformer._();

  String encode(Freshness data) => data.value;

  /// Decodes a [dynamic value][data] to a Freshness.
  ///
  /// If [allowNull] is true and the [dynamic value][data] cannot be decoded successfully,
  /// then null is returned. However, if [allowNull] is false and the [dynamic value][data]
  /// cannot be decoded successfully, then an [UnimplementedError] is thrown.
  ///
  /// The [allowNull] is very handy when an API changes and a new enum value is added or removed,
  /// and users are still using an old app with the old code.
  Freshness? decode(dynamic data, {bool allowNull = true}) {
    if (data != null) {
      switch (data) {
        case r'FRESH': return Freshness.FRESH;
        case r'AGING': return Freshness.AGING;
        case r'STALE': return Freshness.STALE;
        default:
          if (!allowNull) {
            throw ArgumentError('Unknown enum value to decode: $data');
          }
      }
    }
    return null;
  }

  /// Singleton [FreshnessTypeTransformer] instance.
  static FreshnessTypeTransformer? _instance;
}

