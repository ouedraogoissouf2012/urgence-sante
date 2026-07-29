//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class PatientSession {
  /// Returns a new [PatientSession] instance.
  PatientSession({
    required this.patientId,
    required this.token,
  });

  String patientId;

  /// Jeton porteur de session, à conserver côté client et à présenter aux futurs endpoints authentifiés. Renvoyé une seule fois.
  String token;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PatientSession &&
    other.patientId == patientId &&
    other.token == token;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (patientId.hashCode) +
    (token.hashCode);

  @override
  String toString() => 'PatientSession[patientId=$patientId, token=$token]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'patientId'] = this.patientId;
      json[r'token'] = this.token;
    return json;
  }

  /// Returns a new [PatientSession] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static PatientSession? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'patientId'), 'Required key "PatientSession[patientId]" is missing from JSON.');
        assert(json[r'patientId'] != null, 'Required key "PatientSession[patientId]" has a null value in JSON.');
        assert(json.containsKey(r'token'), 'Required key "PatientSession[token]" is missing from JSON.');
        assert(json[r'token'] != null, 'Required key "PatientSession[token]" has a null value in JSON.');
        return true;
      }());

      return PatientSession(
        patientId: mapValueOfType<String>(json, r'patientId')!,
        token: mapValueOfType<String>(json, r'token')!,
      );
    }
    return null;
  }

  static List<PatientSession> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <PatientSession>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = PatientSession.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, PatientSession> mapFromJson(dynamic json) {
    final map = <String, PatientSession>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = PatientSession.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of PatientSession-objects as value to a dart map
  static Map<String, List<PatientSession>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<PatientSession>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = PatientSession.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'patientId',
    'token',
  };
}

