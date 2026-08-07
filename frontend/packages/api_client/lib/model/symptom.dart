//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class Symptom {
  /// Returns a new [Symptom] instance.
  Symptom({
    required this.id,
    required this.label,
  });

  /// Identifiant stable du symptôme (ex. « crise-asthme »).
  String id;

  /// Libellé affiché (ex. « Crise d'asthme »).
  String label;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Symptom &&
    other.id == id &&
    other.label == label;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (id.hashCode) +
    (label.hashCode);

  @override
  String toString() => 'Symptom[id=$id, label=$label]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'id'] = this.id;
      json[r'label'] = this.label;
    return json;
  }

  /// Returns a new [Symptom] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static Symptom? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'id'), 'Required key "Symptom[id]" is missing from JSON.');
        assert(json[r'id'] != null, 'Required key "Symptom[id]" has a null value in JSON.');
        assert(json.containsKey(r'label'), 'Required key "Symptom[label]" is missing from JSON.');
        assert(json[r'label'] != null, 'Required key "Symptom[label]" has a null value in JSON.');
        return true;
      }());

      return Symptom(
        id: mapValueOfType<String>(json, r'id')!,
        label: mapValueOfType<String>(json, r'label')!,
      );
    }
    return null;
  }

  static List<Symptom> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Symptom>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Symptom.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, Symptom> mapFromJson(dynamic json) {
    final map = <String, Symptom>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = Symptom.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of Symptom-objects as value to a dart map
  static Map<String, List<Symptom>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<Symptom>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = Symptom.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'id',
    'label',
  };
}

