import 'package:api_client/api.dart';

import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';

/// Traductions PURES du client généré vers les modèles du domaine.
///
/// Isolées de l'adaptateur (issue #47) pour être testées indépendamment, sans
/// réseau : chaque fonction est déterministe et sans effet de bord.
abstract final class OrientationApiMapper {
  static MedicalNeed toNeed(MedicalService service) =>
      MedicalNeed(code: service.code, label: service.label);

  static List<MedicalNeed> toNeeds(List<MedicalService> services) =>
      services.map(toNeed).toList();

  static RecommendedCenter toCenter(Recommendation r) => RecommendedCenter(
        facilityId: r.facilityId,
        name: r.name,
        latitude: r.location.latitude,
        longitude: r.location.longitude,
        phone: r.phone,
        distanceMeters: r.distanceMeters,
        travelTimeSeconds: r.travelTimeSeconds,
        travelTimeQuality: r.travelTimeQuality.value,
        status: r.status.value,
        explanation: r.explanation,
      );

  static List<RecommendedCenter> toCenters(List<Recommendation> recommendations) =>
      recommendations.map(toCenter).toList();
}
