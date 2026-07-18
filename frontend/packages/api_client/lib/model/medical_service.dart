//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class MedicalService {
  /// Returns a new [MedicalService] instance.
  MedicalService({
    required this.code,
    required this.label,
    this.category,
  });

  /// Code unique et stable (ex. « maternity »).
  String code;

  /// Libellé lisible (ex. « Maternité »).
  String label;

  /// Catégorie de regroupement (ex. « emergency »).
  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? category;

  @override
  bool operator ==(Object other) => identical(this, other) || other is MedicalService &&
    other.code == code &&
    other.label == label &&
    other.category == category;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (code.hashCode) +
    (label.hashCode) +
    (category == null ? 0 : category!.hashCode);

  @override
  String toString() => 'MedicalService[code=$code, label=$label, category=$category]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'code'] = this.code;
      json[r'label'] = this.label;
    if (this.category != null) {
      json[r'category'] = this.category;
    } else {
      json[r'category'] = null;
    }
    return json;
  }

  /// Returns a new [MedicalService] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static MedicalService? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'code'), 'Required key "MedicalService[code]" is missing from JSON.');
        assert(json[r'code'] != null, 'Required key "MedicalService[code]" has a null value in JSON.');
        assert(json.containsKey(r'label'), 'Required key "MedicalService[label]" is missing from JSON.');
        assert(json[r'label'] != null, 'Required key "MedicalService[label]" has a null value in JSON.');
        return true;
      }());

      return MedicalService(
        code: mapValueOfType<String>(json, r'code')!,
        label: mapValueOfType<String>(json, r'label')!,
        category: mapValueOfType<String>(json, r'category'),
      );
    }
    return null;
  }

  static List<MedicalService> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <MedicalService>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = MedicalService.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, MedicalService> mapFromJson(dynamic json) {
    final map = <String, MedicalService>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = MedicalService.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of MedicalService-objects as value to a dart map
  static Map<String, List<MedicalService>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<MedicalService>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = MedicalService.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'code',
    'label',
  };
}

