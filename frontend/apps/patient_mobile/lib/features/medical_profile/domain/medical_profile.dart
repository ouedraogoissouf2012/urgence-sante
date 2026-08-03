/// Groupe sanguin, avec un identifiant STABLE pour le stockage (jamais l'index
/// de l'enum, qui changerait si l'ordre évolue) et un libellé lisible.
enum BloodType {
  oNegative('O-', 'O−'),
  oPositive('O+', 'O+'),
  aNegative('A-', 'A−'),
  aPositive('A+', 'A+'),
  bNegative('B-', 'B−'),
  bPositive('B+', 'B+'),
  abNegative('AB-', 'AB−'),
  abPositive('AB+', 'AB+');

  const BloodType(this.id, this.label);

  /// Identifiant persistant (indépendant de l'ordre de l'enum).
  final String id;

  /// Libellé affiché (le « − » est un vrai signe moins, plus lisible).
  final String label;

  /// Reconstruit depuis un identifiant stocké ; `null` si inconnu/absent.
  static BloodType? fromId(String? id) {
    if (id == null) return null;
    for (final type in values) {
      if (type.id == id) return type;
    }
    return null;
  }
}

/// Fiche médicale d'urgence du patient. TOUS les champs sont facultatifs : le
/// patient renseigne ce qu'il souhaite. Immuable ; jamais transmise au serveur
/// (stockée uniquement sur l'appareil).
class MedicalProfile {
  const MedicalProfile({
    this.fullName,
    this.bloodType,
    this.allergies,
    this.conditions,
    this.treatments,
    this.emergencyContactName,
    this.emergencyContactPhone,
    this.notes,
  });

  /// Nom complet (pour identifier la fiche présentée aux secours).
  final String? fullName;
  final BloodType? bloodType;

  /// Allergies connues (texte libre).
  final String? allergies;

  /// Pathologies / antécédents connus.
  final String? conditions;

  /// Traitements en cours.
  final String? treatments;

  /// Personne à prévenir en cas d'urgence.
  final String? emergencyContactName;
  final String? emergencyContactPhone;

  /// Note libre supplémentaire.
  final String? notes;

  /// Vrai si aucun champ n'est renseigné (les espaces seuls ne comptent pas).
  bool get isEmpty => [
        fullName,
        bloodType?.id,
        allergies,
        conditions,
        treatments,
        emergencyContactName,
        emergencyContactPhone,
        notes,
      ].every((value) => value == null || value.trim().isEmpty);

  bool get isNotEmpty => !isEmpty;

  MedicalProfile copyWith({
    String? fullName,
    BloodType? bloodType,
    String? allergies,
    String? conditions,
    String? treatments,
    String? emergencyContactName,
    String? emergencyContactPhone,
    String? notes,
    bool clearBloodType = false,
  }) {
    return MedicalProfile(
      fullName: fullName ?? this.fullName,
      bloodType: clearBloodType ? null : (bloodType ?? this.bloodType),
      allergies: allergies ?? this.allergies,
      conditions: conditions ?? this.conditions,
      treatments: treatments ?? this.treatments,
      emergencyContactName: emergencyContactName ?? this.emergencyContactName,
      emergencyContactPhone: emergencyContactPhone ?? this.emergencyContactPhone,
      notes: notes ?? this.notes,
    );
  }
}
