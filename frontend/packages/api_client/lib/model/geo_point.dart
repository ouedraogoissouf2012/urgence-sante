//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class GeoPoint {
  /// Returns a new [GeoPoint] instance.
  GeoPoint({
    required this.latitude,
    required this.longitude,
  });

  /// Minimum value: -90
  /// Maximum value: 90
  double latitude;

  /// Minimum value: -180
  /// Maximum value: 180
  double longitude;

  @override
  bool operator ==(Object other) => identical(this, other) || other is GeoPoint &&
    other.latitude == latitude &&
    other.longitude == longitude;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (latitude.hashCode) +
    (longitude.hashCode);

  @override
  String toString() => 'GeoPoint[latitude=$latitude, longitude=$longitude]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'latitude'] = this.latitude;
      json[r'longitude'] = this.longitude;
    return json;
  }

  /// Returns a new [GeoPoint] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static GeoPoint? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'latitude'), 'Required key "GeoPoint[latitude]" is missing from JSON.');
        assert(json[r'latitude'] != null, 'Required key "GeoPoint[latitude]" has a null value in JSON.');
        assert(json.containsKey(r'longitude'), 'Required key "GeoPoint[longitude]" is missing from JSON.');
        assert(json[r'longitude'] != null, 'Required key "GeoPoint[longitude]" has a null value in JSON.');
        return true;
      }());

      return GeoPoint(
        latitude: mapValueOfType<double>(json, r'latitude')!,
        longitude: mapValueOfType<double>(json, r'longitude')!,
      );
    }
    return null;
  }

  static List<GeoPoint> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <GeoPoint>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = GeoPoint.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, GeoPoint> mapFromJson(dynamic json) {
    final map = <String, GeoPoint>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = GeoPoint.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of GeoPoint-objects as value to a dart map
  static Map<String, List<GeoPoint>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<GeoPoint>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = GeoPoint.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'latitude',
    'longitude',
  };
}

