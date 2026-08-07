//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class EmergencyCategory {
  /// Returns a new [EmergencyCategory] instance.
  EmergencyCategory({
    required this.id,
    required this.label,
    required this.directCallOnly,
    this.directCallMessage,
    this.symptoms = const [],
    this.serviceCodes = const [],
  });

  /// Identifiant stable de la catégorie (ex. « respiratoires »).
  String id;

  /// Libellé affiché (ex. « Urgences respiratoires »).
  String label;

  /// Si vrai, l'application affiche un message d'appel direct des secours au lieu de lancer une recherche de centres. 
  bool directCallOnly;

  /// Message affiché lorsque « directCallOnly » est vrai ; absent sinon.
  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? directCallMessage;

  /// Symptômes proposés au second niveau de sélection.
  List<Symptom> symptoms;

  /// Codes des services médicaux « recherchés » (voir MedicalService).
  List<String> serviceCodes;

  @override
  bool operator ==(Object other) => identical(this, other) || other is EmergencyCategory &&
    other.id == id &&
    other.label == label &&
    other.directCallOnly == directCallOnly &&
    other.directCallMessage == directCallMessage &&
    _deepEquality.equals(other.symptoms, symptoms) &&
    _deepEquality.equals(other.serviceCodes, serviceCodes);

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (id.hashCode) +
    (label.hashCode) +
    (directCallOnly.hashCode) +
    (directCallMessage == null ? 0 : directCallMessage!.hashCode) +
    (symptoms.hashCode) +
    (serviceCodes.hashCode);

  @override
  String toString() => 'EmergencyCategory[id=$id, label=$label, directCallOnly=$directCallOnly, directCallMessage=$directCallMessage, symptoms=$symptoms, serviceCodes=$serviceCodes]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'id'] = this.id;
      json[r'label'] = this.label;
      json[r'directCallOnly'] = this.directCallOnly;
    if (this.directCallMessage != null) {
      json[r'directCallMessage'] = this.directCallMessage;
    } else {
      json[r'directCallMessage'] = null;
    }
      json[r'symptoms'] = this.symptoms;
      json[r'serviceCodes'] = this.serviceCodes;
    return json;
  }

  /// Returns a new [EmergencyCategory] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static EmergencyCategory? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'id'), 'Required key "EmergencyCategory[id]" is missing from JSON.');
        assert(json[r'id'] != null, 'Required key "EmergencyCategory[id]" has a null value in JSON.');
        assert(json.containsKey(r'label'), 'Required key "EmergencyCategory[label]" is missing from JSON.');
        assert(json[r'label'] != null, 'Required key "EmergencyCategory[label]" has a null value in JSON.');
        assert(json.containsKey(r'directCallOnly'), 'Required key "EmergencyCategory[directCallOnly]" is missing from JSON.');
        assert(json[r'directCallOnly'] != null, 'Required key "EmergencyCategory[directCallOnly]" has a null value in JSON.');
        assert(json.containsKey(r'symptoms'), 'Required key "EmergencyCategory[symptoms]" is missing from JSON.');
        assert(json[r'symptoms'] != null, 'Required key "EmergencyCategory[symptoms]" has a null value in JSON.');
        assert(json.containsKey(r'serviceCodes'), 'Required key "EmergencyCategory[serviceCodes]" is missing from JSON.');
        assert(json[r'serviceCodes'] != null, 'Required key "EmergencyCategory[serviceCodes]" has a null value in JSON.');
        return true;
      }());

      return EmergencyCategory(
        id: mapValueOfType<String>(json, r'id')!,
        label: mapValueOfType<String>(json, r'label')!,
        directCallOnly: mapValueOfType<bool>(json, r'directCallOnly')!,
        directCallMessage: mapValueOfType<String>(json, r'directCallMessage'),
        symptoms: Symptom.listFromJson(json[r'symptoms']),
        serviceCodes: json[r'serviceCodes'] is Iterable
            ? (json[r'serviceCodes'] as Iterable).cast<String>().toList(growable: false)
            : const [],
      );
    }
    return null;
  }

  static List<EmergencyCategory> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <EmergencyCategory>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = EmergencyCategory.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, EmergencyCategory> mapFromJson(dynamic json) {
    final map = <String, EmergencyCategory>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = EmergencyCategory.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of EmergencyCategory-objects as value to a dart map
  static Map<String, List<EmergencyCategory>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<EmergencyCategory>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = EmergencyCategory.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'id',
    'label',
    'directCallOnly',
    'symptoms',
    'serviceCodes',
  };
}

