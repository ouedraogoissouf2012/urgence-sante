//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

/// Statut de disponibilité d'un service.
class AvailabilityStatus {
  /// Instantiate a new enum with the provided [value].
  const AvailabilityStatus._(this.value);

  /// The underlying value of this enum member.
  final String value;

  @override
  String toString() => value;

  String toJson() => value;

  static const AVAILABLE = AvailabilityStatus._(r'AVAILABLE');
  static const LIMITED = AvailabilityStatus._(r'LIMITED');
  static const SATURATED = AvailabilityStatus._(r'SATURATED');
  static const CLOSED = AvailabilityStatus._(r'CLOSED');
  static const UNKNOWN = AvailabilityStatus._(r'UNKNOWN');

  /// List of all possible values in this [enum][AvailabilityStatus].
  static const values = <AvailabilityStatus>[
    AVAILABLE,
    LIMITED,
    SATURATED,
    CLOSED,
    UNKNOWN,
  ];

  static AvailabilityStatus? fromJson(dynamic value) => AvailabilityStatusTypeTransformer().decode(value);

  static List<AvailabilityStatus> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <AvailabilityStatus>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = AvailabilityStatus.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }
}

/// Transformation class that can [encode] an instance of [AvailabilityStatus] to String,
/// and [decode] dynamic data back to [AvailabilityStatus].
class AvailabilityStatusTypeTransformer {
  factory AvailabilityStatusTypeTransformer() => _instance ??= const AvailabilityStatusTypeTransformer._();

  const AvailabilityStatusTypeTransformer._();

  String encode(AvailabilityStatus data) => data.value;

  /// Decodes a [dynamic value][data] to a AvailabilityStatus.
  ///
  /// If [allowNull] is true and the [dynamic value][data] cannot be decoded successfully,
  /// then null is returned. However, if [allowNull] is false and the [dynamic value][data]
  /// cannot be decoded successfully, then an [UnimplementedError] is thrown.
  ///
  /// The [allowNull] is very handy when an API changes and a new enum value is added or removed,
  /// and users are still using an old app with the old code.
  AvailabilityStatus? decode(dynamic data, {bool allowNull = true}) {
    if (data != null) {
      switch (data) {
        case r'AVAILABLE': return AvailabilityStatus.AVAILABLE;
        case r'LIMITED': return AvailabilityStatus.LIMITED;
        case r'SATURATED': return AvailabilityStatus.SATURATED;
        case r'CLOSED': return AvailabilityStatus.CLOSED;
        case r'UNKNOWN': return AvailabilityStatus.UNKNOWN;
        default:
          if (!allowNull) {
            throw ArgumentError('Unknown enum value to decode: $data');
          }
      }
    }
    return null;
  }

  /// Singleton [AvailabilityStatusTypeTransformer] instance.
  static AvailabilityStatusTypeTransformer? _instance;
}

