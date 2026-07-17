//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class PagedFacilities {
  /// Returns a new [PagedFacilities] instance.
  PagedFacilities({
    this.content = const [],
    required this.page,
  });

  List<Facility> content;

  PageMetadata page;

  @override
  bool operator ==(Object other) => identical(this, other) || other is PagedFacilities &&
    _deepEquality.equals(other.content, content) &&
    other.page == page;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (content.hashCode) +
    (page.hashCode);

  @override
  String toString() => 'PagedFacilities[content=$content, page=$page]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
      json[r'content'] = this.content;
      json[r'page'] = this.page;
    return json;
  }

  /// Returns a new [PagedFacilities] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static PagedFacilities? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        assert(json.containsKey(r'content'), 'Required key "PagedFacilities[content]" is missing from JSON.');
        assert(json[r'content'] != null, 'Required key "PagedFacilities[content]" has a null value in JSON.');
        assert(json.containsKey(r'page'), 'Required key "PagedFacilities[page]" is missing from JSON.');
        assert(json[r'page'] != null, 'Required key "PagedFacilities[page]" has a null value in JSON.');
        return true;
      }());

      return PagedFacilities(
        content: Facility.listFromJson(json[r'content']),
        page: PageMetadata.fromJson(json[r'page'])!,
      );
    }
    return null;
  }

  static List<PagedFacilities> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <PagedFacilities>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = PagedFacilities.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, PagedFacilities> mapFromJson(dynamic json) {
    final map = <String, PagedFacilities>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = PagedFacilities.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of PagedFacilities-objects as value to a dart map
  static Map<String, List<PagedFacilities>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<PagedFacilities>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = PagedFacilities.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
    'content',
    'page',
  };
}

