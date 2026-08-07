import 'package:api_client/api.dart' as api;

import '../domain/model/emergency_category.dart';

/// Traduit les modèles générés du client API vers le domaine de l'app. Pur (sans
/// effet de bord), testé indépendamment. L'import généré est aliasé « api » car
/// les types portent les mêmes noms que le domaine.
class EmergencyTaxonomyMapper {
  const EmergencyTaxonomyMapper._();

  static List<EmergencyCategory> toDomain(List<api.EmergencyCategory> categories) =>
      categories.map(_category).toList(growable: false);

  static EmergencyCategory _category(api.EmergencyCategory category) =>
      EmergencyCategory(
        id: category.id,
        label: category.label,
        directCallOnly: category.directCallOnly,
        directCallMessage: category.directCallMessage,
        symptoms: category.symptoms.map(_symptom).toList(growable: false),
        serviceCodes: List<String>.unmodifiable(category.serviceCodes),
      );

  static Symptom _symptom(api.Symptom symptom) =>
      Symptom(id: symptom.id, label: symptom.label);
}
