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
    this.recenterSeq = 0,
    this.onCenterTap,
    this.controller,
    super.key,
  });

  final double latitude;
  final double longitude;
  final List<RecommendedCenter> centers;
  final String? selectedCenterId;

  /// Jeton d'intention de recentrage (voir `OrientationState.recenterSeq`) :
  /// quand il change, la carte se recentre sur `selectedCenterId`, même si ce
  /// dernier est inchangé.
  final int recenterSeq;

  final ValueChanged<RecommendedCenter>? onCenterTap;

  /// Contrôleur de carte injectable. En production, laissé nul : le widget en
  /// crée un interne. Les tests en fournissent un pour observer la caméra
  /// (`controller.camera.center`) sans dépendre du réseau de tuiles.
  final MapController? controller;

  @override
  State<PositionMap> createState() => _PositionMapState();
}

class _PositionMapState extends State<PositionMap> {
  late final MapController _controller = widget.controller ?? MapController();

  /// Zoom appliqué au recentrage sur un centre.
  static const double _selectedZoom = 15;

  /// Centre à recentrer dès qu'il est disponible dans `centers`. Non nul quand
  /// une intention de recentrage a été émise mais que la cible n'était pas
  /// encore chargée (la sélection peut précéder l'arrivée des données) : la
  /// commande est alors mémorisée et rejouée quand les centres arrivent, plutôt
  /// que perdue silencieusement.
  String? _pendingRecenterId;

  @override
  void initState() {
    super.initState();
    // La carte peut être montée alors qu'un centre est déjà sélectionné (la
    // sélection précède la construction de la vue résultats) : on honore cette
    // intention initiale dès le premier rendu.
    if (widget.recenterSeq > 0 && widget.selectedCenterId != null) {
      _pendingRecenterId = widget.selectedCenterId;
      _tryRecenter();
    }
  }

  @override
  void didUpdateWidget(PositionMap oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Une nouvelle intention de recentrage (jeton modifié) prime : on vise le
    // centre sélectionné courant, même s'il est identique au précédent.
    if (widget.recenterSeq != oldWidget.recenterSeq) {
      _pendingRecenterId = widget.selectedCenterId;
    }
    // Qu'il s'agisse d'une nouvelle intention ou de l'arrivée des centres
    // (course données/sélection), on tente de satisfaire la cible en attente.
    _tryRecenter();
  }

  /// Recentre sur la cible en attente si elle figure dans `centers`, puis
  /// l'efface. Sans correspondance, la cible reste en attente pour un prochain
  /// rebuild (typiquement quand les résultats arrivent). Le déplacement est
  /// différé après le frame courant : `MapController.move` exige que la carte
  /// soit construite, ce qui n'est pas garanti pendant initState/didUpdateWidget.
  void _tryRecenter() {
    final String? target = _pendingRecenterId;
    if (target == null) return;
    for (final center in widget.centers) {
      if (center.facilityId == target) {
        final LatLng destination = LatLng(center.latitude, center.longitude);
        _pendingRecenterId = null;
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _controller.move(destination, _selectedZoom);
        });
        return;
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
