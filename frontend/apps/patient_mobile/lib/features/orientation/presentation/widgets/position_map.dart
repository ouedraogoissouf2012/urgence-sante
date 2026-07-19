import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../domain/model/recommended_center.dart';

/// Carte OpenStreetMap : position de l'utilisateur et centres recommandés.
///
/// Toucher un marqueur sélectionne le centre (synchronisation avec la liste) ;
/// une sélection venue de la liste recentre la carte sur le centre.
class PositionMap extends StatefulWidget {
  const PositionMap({
    required this.latitude,
    required this.longitude,
    this.centers = const [],
    this.selectedCenterId,
    this.onCenterTap,
    super.key,
  });

  final double latitude;
  final double longitude;
  final List<RecommendedCenter> centers;
  final String? selectedCenterId;
  final ValueChanged<RecommendedCenter>? onCenterTap;

  @override
  State<PositionMap> createState() => _PositionMapState();
}

class _PositionMapState extends State<PositionMap> {
  final MapController _controller = MapController();

  @override
  void didUpdateWidget(PositionMap oldWidget) {
    super.didUpdateWidget(oldWidget);
    final String? selected = widget.selectedCenterId;
    if (selected != null && selected != oldWidget.selectedCenterId) {
      for (final center in widget.centers) {
        if (center.facilityId == selected) {
          _controller.move(LatLng(center.latitude, center.longitude), 15);
          break;
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final LatLng position = LatLng(widget.latitude, widget.longitude);
    return FlutterMap(
      mapController: _controller,
      options: MapOptions(initialCenter: position, initialZoom: 13),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'com.urgencesante.patient_mobile',
        ),
        MarkerLayer(
          markers: [
            Marker(
              point: position,
              width: AppSizing.markerTap,
              height: AppSizing.markerTap,
              child: const Icon(Icons.my_location, size: AppSizing.iconMarker),
            ),
            for (final center in widget.centers)
              Marker(
                point: LatLng(center.latitude, center.longitude),
                width: AppSizing.markerTap,
                height: AppSizing.markerTap,
                child: Semantics(
                  button: true,
                  label: 'Centre ${center.name}',
                  child: GestureDetector(
                    onTap: () => widget.onCenterTap?.call(center),
                    child: Icon(
                      Icons.location_pin,
                      size: center.facilityId == widget.selectedCenterId
                          ? AppSizing.markerTap
                          : AppSizing.markerCenter,
                      // Le centre sélectionné adopte l'accent du thème (corail) ;
                      // les autres restent verts (disponibilité).
                      color: center.facilityId == widget.selectedCenterId
                          ? Theme.of(context).colorScheme.primary
                          : AppColors.statusAvailable,
                    ),
                  ),
                ),
              ),
          ],
        ),
      ],
    );
  }
}
