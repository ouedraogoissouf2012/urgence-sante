//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class Recommendation {
  /// Returns a new [Recommendation] instance.
  Recommendation({
    required this.facilityId,
    required this.name,
    required this.location,
    this.phone,
    required this.distanceMeters,
    this.travelTimeSeconds,
    required this.status,
    required this.explanation,
  });

  String facilityId;

  String name;

  GeoPoint location;

  /// Téléphone du centre (absent si inconnu).
  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? phone;

  /// Distance à vol d'oiseau depuis la position du patient.
  double distanceMeters;

  /// Temps de trajet estimé (absent si indisponible).
  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? travelTimeSeconds;

  AvailabilityStatus status;

  /// Raison lisible de la recommandation.
  String explanation;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Recommendation &&
    other.facilityId == facilityId &&
    other.name == name &&
    other.location == location &&
    other.phone == phone &&
    other.distanceMeters == distanceMeters &&
    other.travelTimeSeconds == travelTimeSeconds &&
    other.status == status &&
    other.explanation == explanation;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (facilityId.hashCode) +
    (name.hashCode) +
    (location.hashCode) +
    (phone == null ? 0 : phone!.hashCode) +
    (distanceMeters.hashCode) +
    (travelTimeSeconds == null ? 0 : travelTimeSeconds!.hashCode) +
    (status.hashCode) +
    (explanation.hashCode);

  @override
  String toString() => 'Recommendation[facilityId=$facilityId, name=$name, location=$location, phone=$phone, distanceMeters=$distanceMeters, travelTimeSeconds=$travelTimeSeconds, status=$status, explanation=$explanation]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'facilityId'] = this.facilityId;
      json[r'name'] = this.name;
      json[r'location'] = this.location;
    if (this.phone != null) {
      json[r'phone'] = this.phone;
    } else {
      json[r'phone'] = null;
    }
      json[r'distanceMeters'] = this.distanceMeters;
    if (this.travelTimeSeconds != null) {
      json[r'travelTimeSeconds'] = this.travelTimeSeconds;
    } else {
      json[r'travelTimeSeconds'] = null;
    }
      json[r'status'] = this.status;
      json[r'explanation'] = this.explanation;
    return json;
  }

  /// Returns a new [Recommendation] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static Recommendation? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'facilityId'), 'Required key "Recommendation[facilityId]" is missing from JSON.');
        assert(json[r'facilityId'] != null, 'Required key "Recommendation[facilityId]" has a null value in JSON.');
        assert(json.containsKey(r'name'), 'Required key "Recommendation[name]" is missing from JSON.');
        assert(json[r'name'] != null, 'Required key "Recommendation[name]" has a null value in JSON.');
        assert(json.containsKey(r'location'), 'Required key "Recommendation[location]" is missing from JSON.');
        assert(json[r'location'] != null, 'Required key "Recommendation[location]" has a null value in JSON.');
        assert(json.containsKey(r'distanceMeters'), 'Required key "Recommendation[distanceMeters]" is missing from JSON.');
        assert(json[r'distanceMeters'] != null, 'Required key "Recommendation[distanceMeters]" has a null value in JSON.');
        assert(json.containsKey(r'status'), 'Required key "Recommendation[status]" is missing from JSON.');
        assert(json[r'status'] != null, 'Required key "Recommendation[status]" has a null value in JSON.');
        assert(json.containsKey(r'explanation'), 'Required key "Recommendation[explanation]" is missing from JSON.');
        assert(json[r'explanation'] != null, 'Required key "Recommendation[explanation]" has a null value in JSON.');
        return true;
      }());

      return Recommendation(
        facilityId: mapValueOfType<String>(json, r'facilityId')!,
        name: mapValueOfType<String>(json, r'name')!,
        location: GeoPoint.fromJson(json[r'location'])!,
        phone: mapValueOfType<String>(json, r'phone'),
        distanceMeters: mapValueOfType<double>(json, r'distanceMeters')!,
        travelTimeSeconds: mapValueOfType<double>(json, r'travelTimeSeconds'),
        status: AvailabilityStatus.fromJson(json[r'status'])!,
        explanation: mapValueOfType<String>(json, r'explanation')!,
      );
    }
    return null;
  }

  static List<Recommendation> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Recommendation>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Recommendation.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, Recommendation> mapFromJson(dynamic json) {
    final map = <String, Recommendation>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = Recommendation.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of Recommendation-objects as value to a dart map
  static Map<String, List<Recommendation>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<Recommendation>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = Recommendation.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'facilityId',
    'name',
    'location',
    'distanceMeters',
    'status',
    'explanation',
  };
}

