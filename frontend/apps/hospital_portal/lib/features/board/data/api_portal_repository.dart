import 'package:api_client/api.dart';

import '../domain/model/facility_summary.dart';
import '../domain/model/service_line.dart';
import '../domain/repository/portal_repository.dart';

/// Adaptateur API du portail : fusionne catalogue et disponibilité, sans
/// logique métier au-delà du mapping.
class ApiPortalRepository implements PortalRepository {
  ApiPortalRepository(ApiClient apiClient)
      : _facilitiesApi = FacilitiesApi(apiClient),
        _medicalServicesApi = MedicalServicesApi(apiClient),
        _availabilityApi = AvailabilityApi(apiClient);

  final FacilitiesApi _facilitiesApi;
  final MedicalServicesApi _medicalServicesApi;
  final AvailabilityApi _availabilityApi;

  @override
  Future<List<FacilitySummary>> loadFacilities() async {
    final PagedFacilities? page = await _facilitiesApi.listFacilities(size: 100);
    return (page?.content ?? const [])
        .map((facility) => FacilitySummary(id: facility.id, name: facility.name))
        .toList();
  }

  @override
  Future<List<ServiceLine>> loadBoard(String facilityId) async {
    final List<MedicalService> catalogue =
        await _medicalServicesApi.listMedicalServices() ?? const [];
    final FacilityAvailability? availability =
        await _availabilityApi.getFacilityAvailability(facilityId);
    final Map<String, ServiceAvailability> byCode = {
      for (final service in availability?.services ?? const <ServiceAvailability>[])
        service.serviceCode: service,
    };

    return catalogue.map((service) {
      final ServiceAvailability? current = byCode[service.code];
      return ServiceLine(
        serviceCode: service.code,
        label: service.label,
        status: current?.status.value ?? 'UNKNOWN',
        freshness: current?.freshness.value,
        updatedAt: current?.updatedAt,
      );
    }).toList();
  }

  @override
  Future<ServiceLine> updateStatus({
    required String facilityId,
    required ServiceLine line,
    required String status,
  }) async {
    final ServiceAvailability? updated = await _availabilityApi.updateAvailability(
      facilityId,
      line.serviceCode,
      UpdateAvailabilityRequest(status: AvailabilityStatus.fromJson(status)!),
    );
    return line.withUpdate(
      status: updated?.status.value ?? status,
      freshness: updated?.freshness.value,
      updatedAt: updated?.updatedAt ?? DateTime.now().toUtc(),
    );
  }

  @override
  Future<List<HistoryEntry>> history(String facilityId, String serviceCode) async {
    final List<AvailabilityHistoryEntry> entries =
        await _availabilityApi.getAvailabilityHistory(facilityId, serviceCode) ?? const [];
    return entries
        .map((entry) => HistoryEntry(status: entry.status.value, updatedAt: entry.updatedAt))
        .toList();
  }
}
