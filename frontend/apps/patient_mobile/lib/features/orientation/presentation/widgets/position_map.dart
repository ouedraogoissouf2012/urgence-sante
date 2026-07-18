import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

/// Carte OpenStreetMap centrée sur la position de l'utilisateur.
class PositionMap extends StatelessWidget {
  const PositionMap({required this.latitude, required this.longitude, super.key});

  final double latitude;
  final double longitude;

  @override
  Widget build(BuildContext context) {
    final LatLng position = LatLng(latitude, longitude);
    return FlutterMap(
      options: MapOptions(initialCenter: position, initialZoom: 14),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'com.urgencesante.patient_mobile',
        ),
        MarkerLayer(
          markers: [
            Marker(
              point: position,
              width: 44,
              height: 44,
              child: const Icon(Icons.my_location, size: 36),
            ),
          ],
        ),
      ],
    );
  }
}
