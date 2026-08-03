import 'dart:convert';

import '../domain/medical_profile.dart';

/// Sérialisation JSON de la fiche médicale (stockage local).
///
/// Format versionné : {"version": 1, ...champs}. Toute erreur de décodage est
/// traitée comme fiche absente (jamais de crash sur données corrompues). Le
/// groupe sanguin est stocké par son identifiant stable, un identifiant inconnu
/// est simplement ignoré.
abstract final class MedicalProfileCodec {
  /// Version du format, incrémentée si la structure évolue.
  static const int version = 1;

  static String encode(MedicalProfile profile) {
    return jsonEncode({
      'version': version,
      if (profile.fullName != null) 'fullName': profile.fullName,
      if (profile.bloodType != null) 'bloodType': profile.bloodType!.id,
      if (profile.allergies != null) 'allergies': profile.allergies,
      if (profile.conditions != null) 'conditions': profile.conditions,
      if (profile.treatments != null) 'treatments': profile.treatments,
      if (profile.emergencyContactName != null)
        'emergencyContactName': profile.emergencyContactName,
      if (profile.emergencyContactPhone != null)
        'emergencyContactPhone': profile.emergencyContactPhone,
      if (profile.notes != null) 'notes': profile.notes,
    });
  }

  /// Décode la fiche ; renvoie une fiche vide si absente ou corrompue.
  static MedicalProfile decode(String? raw) {
    if (raw == null || raw.isEmpty) {
      return const MedicalProfile();
    }
    try {
      final map = jsonDecode(raw) as Map<String, dynamic>;
      return MedicalProfile(
        fullName: _string(map['fullName']),
        bloodType: BloodType.fromId(_string(map['bloodType'])),
        allergies: _string(map['allergies']),
        conditions: _string(map['conditions']),
        treatments: _string(map['treatments']),
        emergencyContactName: _string(map['emergencyContactName']),
        emergencyContactPhone: _string(map['emergencyContactPhone']),
        notes: _string(map['notes']),
      );
    } on Object {
      // JSON invalide, type inattendu… : on n'expose jamais une exception.
      return const MedicalProfile();
    }
  }

  static String? _string(Object? value) => value is String ? value : null;
}
