//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class RegisterPatientRequest {
  /// Returns a new [RegisterPatientRequest] instance.
  RegisterPatientRequest({
    required this.phone,
    required this.password,
  });

  /// Numéro de téléphone au format international (ex. +225XXXXXXXXXX).
  String phone;

  /// Mot de passe (au moins 8 caractères). Jamais renvoyé ni stocké en clair.
  String password;

  @override
  bool operator ==(Object other) => identical(this, other) || other is RegisterPatientRequest &&
    other.phone == phone &&
    other.password == password;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (phone.hashCode) +
    (password.hashCode);

  @override
  String toString() => 'RegisterPatientRequest[phone=$phone, password=$password]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'phone'] = this.phone;
      json[r'password'] = this.password;
    return json;
  }

  /// Returns a new [RegisterPatientRequest] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static RegisterPatientRequest? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'phone'), 'Required key "RegisterPatientRequest[phone]" is missing from JSON.');
        assert(json[r'phone'] != null, 'Required key "RegisterPatientRequest[phone]" has a null value in JSON.');
        assert(json.containsKey(r'password'), 'Required key "RegisterPatientRequest[password]" is missing from JSON.');
        assert(json[r'password'] != null, 'Required key "RegisterPatientRequest[password]" has a null value in JSON.');
        return true;
      }());

      return RegisterPatientRequest(
        phone: mapValueOfType<String>(json, r'phone')!,
        password: mapValueOfType<String>(json, r'password')!,
      );
    }
    return null;
  }

  static List<RegisterPatientRequest> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <RegisterPatientRequest>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = RegisterPatientRequest.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, RegisterPatientRequest> mapFromJson(dynamic json) {
    final map = <String, RegisterPatientRequest>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = RegisterPatientRequest.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of RegisterPatientRequest-objects as value to a dart map
  static Map<String, List<RegisterPatientRequest>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<RegisterPatientRequest>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = RegisterPatientRequest.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'phone',
    'password',
  };
}

