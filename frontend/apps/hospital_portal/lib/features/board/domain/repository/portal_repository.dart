import '../model/facility_summary.dart';
import '../model/service_line.dart';

/// Contrat de données du portail hospitalier. Substituable par un faux en test.
abstract interface class PortalRepository {
  /// Établissements sélectionnables (accès démo).
  Future<List<FacilitySummary>> loadFacilities();

  /// Tableau des services de l'établissement : catalogue fusionné avec la
  /// disponibilité courante (« UNKNOWN » si jamais renseignée).
  Future<List<ServiceLine>> loadBoard(String facilityId);

  /// Met à jour le statut d'un service et retourne la ligne actualisée.
  Future<ServiceLine> updateStatus({
    required String facilityId,
    required ServiceLine line,
    required String status,
  });

  /// Historique des mises à jour d'un service (récent d'abord).
  Future<List<HistoryEntry>> history(String facilityId, String serviceCode);
}
