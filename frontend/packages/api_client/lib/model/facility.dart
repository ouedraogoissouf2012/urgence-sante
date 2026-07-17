//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class Facility {
  /// Returns a new [Facility] instance.
  Facility({
    required this.id,
    required this.name,
    required this.location,
    this.phone,
    this.services = const [],
    this.distanceMeters,
  });

  String id;

  String name;

  GeoPoint location;

  /// Numéro de contact, au format international.
  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? phone;

  /// Codes des services médicaux offerts.
  List<String> services;

  /// Distance depuis le point de recherche, en mètres. Présent uniquement lorsque la requête fournit une position. 
  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  int? distanceMeters;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Facility &&
    other.id == id &&
    other.name == name &&
    other.location == location &&
    other.phone == phone &&
    _deepEquality.equals(other.services, services) &&
    other.distanceMeters == distanceMeters;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (id.hashCode) +
    (name.hashCode) +
    (location.hashCode) +
    (phone == null ? 0 : phone!.hashCode) +
    (services.hashCode) +
    (distanceMeters == null ? 0 : distanceMeters!.hashCode);

  @override
  String toString() => 'Facility[id=$id, name=$name, location=$location, phone=$phone, services=$services, distanceMeters=$distanceMeters]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'id'] = this.id;
      json[r'name'] = this.name;
      json[r'location'] = this.location;
    if (this.phone != null) {
      json[r'phone'] = this.phone;
    } else {
      json[r'phone'] = null;
    }
      json[r'services'] = this.services;
    if (this.distanceMeters != null) {
      json[r'distanceMeters'] = this.distanceMeters;
    } else {
      json[r'distanceMeters'] = null;
    }
    return json;
  }

  /// Returns a new [Facility] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static Facility? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'id'), 'Required key "Facility[id]" is missing from JSON.');
        assert(json[r'id'] != null, 'Required key "Facility[id]" has a null value in JSON.');
        assert(json.containsKey(r'name'), 'Required key "Facility[name]" is missing from JSON.');
        assert(json[r'name'] != null, 'Required key "Facility[name]" has a null value in JSON.');
        assert(json.containsKey(r'location'), 'Required key "Facility[location]" is missing from JSON.');
        assert(json[r'location'] != null, 'Required key "Facility[location]" has a null value in JSON.');
        assert(json.containsKey(r'services'), 'Required key "Facility[services]" is missing from JSON.');
        assert(json[r'services'] != null, 'Required key "Facility[services]" has a null value in JSON.');
        return true;
      }());

      return Facility(
        id: mapValueOfType<String>(json, r'id')!,
        name: mapValueOfType<String>(json, r'name')!,
        location: GeoPoint.fromJson(json[r'location'])!,
        phone: mapValueOfType<String>(json, r'phone'),
        services: json[r'services'] is Iterable
            ? (json[r'services'] as Iterable).cast<String>().toList(growable: false)
            : const [],
        distanceMeters: mapValueOfType<int>(json, r'distanceMeters'),
      );
    }
    return null;
  }

  static List<Facility> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Facility>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Facility.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, Facility> mapFromJson(dynamic json) {
    final map = <String, Facility>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = Facility.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of Facility-objects as value to a dart map
  static Map<String, List<Facility>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<Facility>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = Facility.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'id',
    'name',
    'location',
    'services',
  };
}

