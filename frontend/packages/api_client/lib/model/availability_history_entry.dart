//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class AvailabilityHistoryEntry {
  /// Returns a new [AvailabilityHistoryEntry] instance.
  AvailabilityHistoryEntry({
    required this.status,
    required this.updatedAt,
  });

  AvailabilityStatus status;

  DateTime updatedAt;

  @override
  bool operator ==(Object other) => identical(this, other) || other is AvailabilityHistoryEntry &&
    other.status == status &&
    other.updatedAt == updatedAt;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (status.hashCode) +
    (updatedAt.hashCode);

  @override
  String toString() => 'AvailabilityHistoryEntry[status=$status, updatedAt=$updatedAt]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'status'] = this.status;
      json[r'updatedAt'] = this.updatedAt.toUtc().toIso8601String();
    return json;
  }

  /// Returns a new [AvailabilityHistoryEntry] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static AvailabilityHistoryEntry? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'status'), 'Required key "AvailabilityHistoryEntry[status]" is missing from JSON.');
        assert(json[r'status'] != null, 'Required key "AvailabilityHistoryEntry[status]" has a null value in JSON.');
        assert(json.containsKey(r'updatedAt'), 'Required key "AvailabilityHistoryEntry[updatedAt]" is missing from JSON.');
        assert(json[r'updatedAt'] != null, 'Required key "AvailabilityHistoryEntry[updatedAt]" has a null value in JSON.');
        return true;
      }());

      return AvailabilityHistoryEntry(
        status: AvailabilityStatus.fromJson(json[r'status'])!,
        updatedAt: mapDateTime(json, r'updatedAt', r'')!,
      );
    }
    return null;
  }

  static List<AvailabilityHistoryEntry> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <AvailabilityHistoryEntry>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = AvailabilityHistoryEntry.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, AvailabilityHistoryEntry> mapFromJson(dynamic json) {
    final map = <String, AvailabilityHistoryEntry>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = AvailabilityHistoryEntry.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of AvailabilityHistoryEntry-objects as value to a dart map
  static Map<String, List<AvailabilityHistoryEntry>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<AvailabilityHistoryEntry>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = AvailabilityHistoryEntry.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'status',
    'updatedAt',
  };
}

