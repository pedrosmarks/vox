import 'dart:convert';

import 'package:http/http.dart' as http;

import 'api_client.dart';

class DashboardService {
  Future<Map<String, dynamic>> load({
    required String from,
    required String to,
  }) async {
    final results = await Future.wait([
      _entry('overview', 'overview', from, to),
      _entry('moderation', 'moderacao', from, to),
      _entry('engagement', 'engajamento', from, to),
      _entry('categories', 'categorias', from, to),
      _entry(
        'timeline',
        'series-temporais',
        from,
        to,
        extra: {'granularidade': 'month'},
      ),
      _entry('neighborhoods', 'mapa/bairros', from, to),
      _entry(
        'hotspots',
        'mapa/coordenadas',
        from,
        to,
        extra: {'precision': '3'},
      ),
      _entry('projectLifecycle', 'projetos/ciclo-vida', from, to),
    ]);
    return Map<String, dynamic>.fromEntries(results);
  }

  Future<MapEntry<String, dynamic>> _entry(
    String key,
    String endpoint,
    String from,
    String to, {
    Map<String, String> extra = const {},
  }) async {
    try {
      final uri = Uri.parse(
        '${ApiClient.baseUrl}/api/admin/dashboard/$endpoint',
      ).replace(queryParameters: {'from': from, 'to': to, ...extra});
      final response = await http.get(
        uri,
        headers: await ApiClient.authHeaders(),
      );
      ApiClient.checkResponse(response);
      final decoded = jsonDecode(response.body);
      return MapEntry(key, decoded);
    } catch (_) {
      return MapEntry(key, null);
    }
  }
}
