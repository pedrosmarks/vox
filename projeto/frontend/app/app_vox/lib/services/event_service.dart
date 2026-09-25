import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/event.dart';
import 'api_client.dart';

class EventService {
  static const _base = '${ApiClient.baseUrl}/api';
  Future<EventPage> getEvents({
    String? search,
    int? categoryId,
    int? municipalityId,
    bool free = false,
    int page = 0,
    int size = 12,
  }) async {
    final query = <String, String>{'page': '$page', 'size': '$size'};
    if (search != null && search.trim().isNotEmpty) {
      query['search'] = search.trim();
    }
    if (categoryId != null) query['categoryId'] = '$categoryId';
    if (municipalityId != null) query['municipalityId'] = '$municipalityId';
    if (free) query['free'] = 'true';
    final response = await http.get(
      Uri.parse('$_base/events').replace(queryParameters: query),
      headers: await ApiClient.authHeaders(json: false),
    );
    ApiClient.checkResponse(response);
    return EventPage.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<CivicEvent> getEvent(int id) async {
    final response = await http.get(
      Uri.parse('$_base/events/$id'),
      headers: await ApiClient.authHeaders(json: false),
    );
    ApiClient.checkResponse(response);
    return CivicEvent.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<EventImage>> getEventImages(int id) async {
    final response = await http.get(
      Uri.parse('$_base/events/$id/images'),
      headers: await ApiClient.authHeaders(json: false),
    );
    ApiClient.checkResponse(response);
    return (jsonDecode(response.body) as List<dynamic>)
        .whereType<Map<String, dynamic>>()
        .map(EventImage.fromJson)
        .toList();
  }

  Future<List<Map<String, dynamic>>> getCategories() async {
    final response = await http.get(
      Uri.parse('$_base/event-categories'),
      headers: await ApiClient.authHeaders(json: false),
    );
    ApiClient.checkResponse(response);
    return (jsonDecode(response.body) as List<dynamic>)
        .whereType<Map<String, dynamic>>()
        .toList();
  }

  Future<int?> createEvent(Map<String, String> fields) async {
    final streamed = await ApiClient.multipartRequest(
      'POST',
      '$_base/events',
      _nonEmptyFields(fields),
    );
    final response = await http.Response.fromStream(streamed);
    ApiClient.checkResponse(response);
    return ApiClient.locationId(response);
  }

  Future<void> updateEvent(int id, Map<String, String> fields) async {
    final streamed = await ApiClient.multipartRequest(
      'PUT',
      '$_base/events/$id',
      _nonEmptyFields(fields),
    );
    ApiClient.checkResponse(await http.Response.fromStream(streamed));
  }

  Future<void> deleteEvent(int id) async {
    final response = await http.delete(
      Uri.parse('$_base/events/$id'),
      headers: await ApiClient.authHeaders(json: false),
    );
    ApiClient.checkResponse(response);
  }

  Map<String, String> _nonEmptyFields(Map<String, String> fields) =>
      Map.fromEntries(
        fields.entries.where((entry) => entry.value.trim().isNotEmpty),
      );
}
