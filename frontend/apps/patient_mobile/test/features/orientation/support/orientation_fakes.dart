import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/medical_need.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';

/// Doublures de test substituables aux vrais collaborateurs (LSP), partagées
/// par les suites du view-model d'orientation.

/// Faux repository configurable, substituable au vrai adaptateur API.
class FakeOrientationRepository implements OrientationRepository {
  List<MedicalNeed> needs = const [MedicalNeed(code: 'maternity', label: 'Maternité')];
  List<RecommendedCenter> results = const [];
  bool failNeeds = false;
  bool failRecommend = false;
  Cached<List<MedicalNeed>>? cachedNeeds;
  Cached<List<RecommendedCenter>>? knownCenters;

  @override
  Future<Cached<List<MedicalNeed>>> loadNeeds() async {
    if (failNeeds) {
      final fallback = cachedNeeds;
      if (fallback != null) return fallback;
      throw Exception('réseau');
    }
    return Cached.live(needs);
  }

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  }) async {
    if (failRecommend) throw Exception('réseau');
    return results;
  }

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters() async => knownCenters;
}

/// Fausse localisation, substituable à Geolocator.
class FakeLocationService implements LocationService {
  bool denied = false;
  LocationFailure failure = LocationFailure.denied;
  final List<LocationFailure> settingsOpened = [];

  @override
  Future<UserPosition> currentPosition() async {
    if (denied) {
      throw LocationUnavailableException('Autorisation refusée.', failure);
    }
    return const UserPosition(latitude: 5.35, longitude: -4.0);
  }

  @override
  Future<void> openSettings(LocationFailure failure) async =>
      settingsOpened.add(failure);
}
