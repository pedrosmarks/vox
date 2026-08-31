import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:http/http.dart' as http;
import 'package:latlong2/latlong.dart';
import '../theme/vox_colors.dart';

/// Endereço obtido por geocodificação reversa (Nominatim/OpenStreetMap),
/// igual ao AddressResult do MapPickerComponent do site.
class MapAddress {
  final String street;
  final String number;
  final String neighborhood;

  const MapAddress({
    this.street = '',
    this.number = '',
    this.neighborhood = '',
  });
}

/// Mapa para selecionar a localização de um projeto/ocorrência, equivalente
/// ao MapPickerComponent (maplibre) do site — aqui usando flutter_map/OSM.
class MapPickerField extends StatefulWidget {
  final double? initialLatitude;
  final double? initialLongitude;
  final ValueChanged<LatLng> onLocationChanged;
  final ValueChanged<MapAddress>? onAddressChanged;

  const MapPickerField({
    super.key,
    this.initialLatitude,
    this.initialLongitude,
    required this.onLocationChanged,
    this.onAddressChanged,
  });

  @override
  State<MapPickerField> createState() => _MapPickerFieldState();
}

class _MapPickerFieldState extends State<MapPickerField> {
  // Centro padrão: Brasília (mesmo fallback usado no site).
  static const _defaultCenter = LatLng(-15.7801, -47.9292);

  final _mapController = MapController();
  LatLng? _selected;
  bool _isGeocoding = false;

  @override
  void initState() {
    super.initState();
    if (widget.initialLatitude != null && widget.initialLongitude != null) {
      _selected = LatLng(widget.initialLatitude!, widget.initialLongitude!);
    }
  }

  Future<void> _selectPoint(LatLng point) async {
    setState(() => _selected = point);
    widget.onLocationChanged(point);
    if (widget.onAddressChanged == null) return;

    setState(() => _isGeocoding = true);
    try {
      final uri = Uri.parse(
        'https://nominatim.openstreetmap.org/reverse'
        '?lat=${point.latitude}&lon=${point.longitude}&format=json&accept-language=pt-BR',
      );
      final response = await http.get(
        uri,
        headers: {'User-Agent': 'vox-cidadao-app'},
      );
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        final addr = (data['address'] as Map<String, dynamic>?) ?? {};
        widget.onAddressChanged!(
          MapAddress(
            street:
                (addr['road'] ?? addr['pedestrian'] ?? addr['footway'] ?? '')
                    as String,
            number: (addr['house_number'] ?? '') as String,
            neighborhood:
                (addr['suburb'] ??
                        addr['neighbourhood'] ??
                        addr['quarter'] ??
                        addr['village'] ??
                        addr['town'] ??
                        '')
                    as String,
          ),
        );
      }
    } catch (_) {
      // segue sem preencher o endereço automaticamente
    } finally {
      if (mounted) setState(() => _isGeocoding = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: Container(
            height: 260,
            decoration: BoxDecoration(
              border: Border.all(color: VoxColors.border, width: 2),
            ),
            child: FlutterMap(
              mapController: _mapController,
              options: MapOptions(
                initialCenter: _selected ?? _defaultCenter,
                initialZoom: _selected != null ? 15 : 4,
                onTap: (_, point) => _selectPoint(point),
              ),
              children: [
                TileLayer(
                  urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                  userAgentPackageName: 'com.vox.app_vox',
                ),
                if (_selected != null)
                  MarkerLayer(
                    markers: [
                      Marker(
                        point: _selected!,
                        width: 40,
                        height: 40,
                        child: const Icon(
                          Icons.location_pin,
                          color: VoxColors.accent,
                          size: 40,
                        ),
                      ),
                    ],
                  ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 6),
        Text(
          _isGeocoding
              ? '🔍 Buscando endereço...'
              : _selected == null
              ? 'Toque no mapa para selecionar a localização'
              : '📍 Lat: ${_selected!.latitude.toStringAsFixed(6)}  '
                    'Lng: ${_selected!.longitude.toStringAsFixed(6)}',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w600,
            color: _selected == null ? VoxColors.textMuted : VoxColors.accent,
          ),
        ),
      ],
    );
  }
}
