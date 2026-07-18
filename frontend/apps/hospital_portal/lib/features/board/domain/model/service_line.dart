/// Ligne du tableau de disponibilité : un service et son état courant.
class ServiceLine {
  const ServiceLine({
    required this.serviceCode,
    required this.label,
    required this.status,
    this.freshness,
    this.updatedAt,
  });

  final String serviceCode;
  final String label;

  /// Statut du contrat (« AVAILABLE »…) ; « UNKNOWN » si jamais renseigné.
  final String status;
  final String? freshness;
  final DateTime? updatedAt;

  ServiceLine withUpdate({required String status, required DateTime updatedAt, String? freshness}) {
    return ServiceLine(
      serviceCode: serviceCode,
      label: label,
      status: status,
      freshness: freshness,
      updatedAt: updatedAt,
    );
  }
}

/// Entrée d'historique d'un service.
class HistoryEntry {
  const HistoryEntry({required this.status, required this.updatedAt});

  final String status;
  final DateTime updatedAt;
}
