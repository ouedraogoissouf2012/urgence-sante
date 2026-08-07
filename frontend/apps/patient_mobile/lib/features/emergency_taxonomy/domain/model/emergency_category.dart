/// Symptôme sélectionnable au second niveau de la taxonomie des urgences.
/// L'identité repose sur [id] (identifiant stable) ; [label] est affiché.
class Symptom {
  const Symptom({required this.id, required this.label});

  final String id;
  final String label;

  @override
  bool operator ==(Object other) => other is Symptom && other.id == id;

  @override
  int get hashCode => id.hashCode;
}

/// Grande catégorie d'urgence (premier niveau de sélection), miroir du contrat.
///
/// Certaines catégories relèvent d'un **appel direct des secours**
/// ([directCallOnly]) : l'application affiche alors [directCallMessage] au lieu
/// de lancer une recherche de centres. Sinon, [serviceCodes] alimente la
/// recherche multi-services.
class EmergencyCategory {
  const EmergencyCategory({
    required this.id,
    required this.label,
    required this.directCallOnly,
    this.directCallMessage,
    this.symptoms = const [],
    this.serviceCodes = const [],
  });

  final String id;
  final String label;
  final bool directCallOnly;
  final String? directCallMessage;
  final List<Symptom> symptoms;
  final List<String> serviceCodes;

  @override
  bool operator ==(Object other) => other is EmergencyCategory && other.id == id;

  @override
  int get hashCode => id.hashCode;
}
