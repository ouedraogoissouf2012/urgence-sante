import 'package:patient_mobile/core/location/location_service.dart';
import 'package:patient_mobile/features/orientation/domain/model/cached.dart';
import 'package:patient_mobile/features/orientation/domain/model/recommended_center.dart';
import 'package:patient_mobile/features/orientation/domain/repository/orientation_repository.dart';

/// Doublures de test substituables aux vrais collaborateurs (LSP), partagées
/// par les suites du view-model d'orientation.

/// Faux repository configurable, substituable au vrai adaptateur API.
class FakeOrientationRepository implements OrientationRepository {
  List<RecommendedCenter> results = const [];
  bool failRecommend = false;
  Cached<List<RecommendedCenter>>? knownCenters;

  /// Dernier besoin pour lequel le repli hors ligne a été interrogé : permet de
  /// vérifier que le view-model cloisonne bien le repli sur le besoin courant.
  List<String>? lastKnownCentersServiceCodes;

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async {
    if (failRecommend) throw Exception('réseau');
    return results;
  }

  @override
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters({
    required List<String> serviceCodes,
  }) async {
    lastKnownCentersServiceCodes = serviceCodes;
    return knownCenters;
  }
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
