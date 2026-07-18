//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class Route {
  /// Returns a new [Route] instance.
  Route({
    required this.distanceMeters,
    required this.durationSeconds,
  });

  /// Distance de l'itinéraire en mètres.
  double distanceMeters;

  /// Durée estimée du trajet en secondes.
  double durationSeconds;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Route &&
    other.distanceMeters == distanceMeters &&
    other.durationSeconds == durationSeconds;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (distanceMeters.hashCode) +
    (durationSeconds.hashCode);

  @override
  String toString() => 'Route[distanceMeters=$distanceMeters, durationSeconds=$durationSeconds]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'distanceMeters'] = this.distanceMeters;
      json[r'durationSeconds'] = this.durationSeconds;
    return json;
  }

  /// Returns a new [Route] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static Route? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'distanceMeters'), 'Required key "Route[distanceMeters]" is missing from JSON.');
        assert(json[r'distanceMeters'] != null, 'Required key "Route[distanceMeters]" has a null value in JSON.');
        assert(json.containsKey(r'durationSeconds'), 'Required key "Route[durationSeconds]" is missing from JSON.');
        assert(json[r'durationSeconds'] != null, 'Required key "Route[durationSeconds]" has a null value in JSON.');
        return true;
      }());

      return Route(
        distanceMeters: mapValueOfType<double>(json, r'distanceMeters')!,
        durationSeconds: mapValueOfType<double>(json, r'durationSeconds')!,
      );
    }
    return null;
  }

  static List<Route> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Route>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Route.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, Route> mapFromJson(dynamic json) {
    final map = <String, Route>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = Route.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of Route-objects as value to a dart map
  static Map<String, List<Route>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<Route>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = Route.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'distanceMeters',
    'durationSeconds',
  };
}

