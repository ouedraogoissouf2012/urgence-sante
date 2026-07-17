//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class PageMetadata {
  /// Returns a new [PageMetadata] instance.
  PageMetadata({
    required this.number,
    required this.size,
    required this.totalElements,
    required this.totalPages,
  });

  /// Index de la page courante (à partir de 0).
  int number;

  /// Taille de page demandée.
  int size;

  /// Nombre total d'éléments disponibles.
  int totalElements;

  /// Nombre total de pages.
  int totalPages;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PageMetadata &&
    other.number == number &&
    other.size == size &&
    other.totalElements == totalElements &&
    other.totalPages == totalPages;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (number.hashCode) +
    (size.hashCode) +
    (totalElements.hashCode) +
    (totalPages.hashCode);

  @override
  String toString() => 'PageMetadata[number=$number, size=$size, totalElements=$totalElements, totalPages=$totalPages]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'number'] = this.number;
      json[r'size'] = this.size;
      json[r'totalElements'] = this.totalElements;
      json[r'totalPages'] = this.totalPages;
    return json;
  }

  /// Returns a new [PageMetadata] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static PageMetadata? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'number'), 'Required key "PageMetadata[number]" is missing from JSON.');
        assert(json[r'number'] != null, 'Required key "PageMetadata[number]" has a null value in JSON.');
        assert(json.containsKey(r'size'), 'Required key "PageMetadata[size]" is missing from JSON.');
        assert(json[r'size'] != null, 'Required key "PageMetadata[size]" has a null value in JSON.');
        assert(json.containsKey(r'totalElements'), 'Required key "PageMetadata[totalElements]" is missing from JSON.');
        assert(json[r'totalElements'] != null, 'Required key "PageMetadata[totalElements]" has a null value in JSON.');
        assert(json.containsKey(r'totalPages'), 'Required key "PageMetadata[totalPages]" is missing from JSON.');
        assert(json[r'totalPages'] != null, 'Required key "PageMetadata[totalPages]" has a null value in JSON.');
        return true;
      }());

      return PageMetadata(
        number: mapValueOfType<int>(json, r'number')!,
        size: mapValueOfType<int>(json, r'size')!,
        totalElements: mapValueOfType<int>(json, r'totalElements')!,
        totalPages: mapValueOfType<int>(json, r'totalPages')!,
      );
    }
    return null;
  }

  static List<PageMetadata> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <PageMetadata>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = PageMetadata.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, PageMetadata> mapFromJson(dynamic json) {
    final map = <String, PageMetadata>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = PageMetadata.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of PageMetadata-objects as value to a dart map
  static Map<String, List<PageMetadata>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<PageMetadata>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = PageMetadata.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'number',
    'size',
    'totalElements',
    'totalPages',
  };
}

