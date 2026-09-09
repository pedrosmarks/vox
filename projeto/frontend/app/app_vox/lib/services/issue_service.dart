import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_client.dart';
import '../models/issue.dart';

class IssueService {
  Future<List<IssueReport>> getIssues({int? page, int? size}) async {
    final params = <String>[];
    if (page != null) params.add('page=$page');
    if (size != null) params.add('size=$size');
    final query = params.isNotEmpty ? '?${params.join('&')}' : '';
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/issues$query'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => IssueReport.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<IssueReport> getIssueById(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/issues/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    return IssueReport.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<IssueReport>> getMyIssues() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/issues/my'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => IssueReport.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<IssueReport> createIssue(
    Map<String, String> fields, {
    List<http.MultipartFile> files = const [],
  }) async {
    final streamed = await ApiClient.multipartRequest(
      'POST',
      '${ApiClient.baseUrl}/api/issues',
      fields,
      files: files,
    );
    final response = await http.Response.fromStream(streamed);
    ApiClient.checkResponse(response);
    // Backend responde 201 Created só com header Location, sem corpo.
    if (response.body.trim().isEmpty) {
      final id = ApiClient.locationId(response);
      if (id != null) return getIssueById(id);
      // Sem header Location: busca a ocorrência mais recente do autor.
      final mine = await getMyIssues();
      if (mine.isNotEmpty) {
        mine.sort((a, b) => b.id.compareTo(a.id));
        return mine.first;
      }
      throw ApiException(
        'Ocorrência criada, mas não foi possível carregar os dados.',
      );
    }
    return IssueReport.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> updateIssue(
    int id,
    Map<String, String> fields, {
    List<http.MultipartFile> files = const [],
  }) async {
    final streamed = await ApiClient.multipartRequest(
      'PUT',
      '${ApiClient.baseUrl}/api/issues/$id',
      fields,
      files: files,
    );
    final response = await http.Response.fromStream(streamed);
    ApiClient.checkResponse(response);
  }

  Future<void> deleteIssue(int id) async {
    final response = await http.delete(
      Uri.parse('${ApiClient.baseUrl}/api/issues/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<List<IssueStatusHistory>> getIssueHistory(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/issues/$id/history'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => IssueStatusHistory.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<List<IssueImage>> getIssueImages(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/issues/$id/images'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => IssueImage.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> addIssueImage(
    int id, {
    required List<http.MultipartFile> files,
  }) async {
    final streamed = await ApiClient.multipartRequest(
      'POST',
      '${ApiClient.baseUrl}/api/issues/$id/images',
      const {},
      files: files,
    );
    final response = await http.Response.fromStream(streamed);
    ApiClient.checkResponse(response);
  }

  Future<void> deleteIssueImage(int id, int imageId) async {
    final response = await http.delete(
      Uri.parse('${ApiClient.baseUrl}/api/issues/$id/images/$imageId'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<List<IssueReport>> getPendingIssues({int? page, int? size}) async {
    final params = <String>[];
    if (page != null) params.add('page=$page');
    if (size != null) params.add('size=$size');
    final query = params.isNotEmpty ? '?${params.join('&')}' : '';
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/issues/pending$query'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => IssueReport.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> approveIssue(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/issues/$id/approve'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> rejectIssue(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/issues/$id/reject'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  /// Atualiza a ocorrência enviando o objeto completo (JSON), conforme API.md.
  Future<void> updateIssueJson(IssueReport issue, {int? councilorId}) async {
    final response = await http.put(
      Uri.parse('${ApiClient.baseUrl}/api/issues/${issue.id}'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode({
        'id': issue.id,
        'title': issue.title,
        'description': issue.description,
        'municipalityId': issue.municipalityId,
        'categoryId': issue.categoryId,
        'status': issue.status,
        'authorId': issue.authorId,
        'councilorId': councilorId,
        'neighborhood': issue.neighborhood,
        'street': issue.street,
        'number': issue.number,
        'latitude': issue.latitude,
        'longitude': issue.longitude,
      }),
    );
    ApiClient.checkResponse(response);
  }

  /// Vincula/atribui a ocorrência a um vereador (ou remove com null).
  Future<void> assignCouncilor(IssueReport issue, int? councilorId) =>
      updateIssueJson(issue, councilorId: councilorId);

  /// Atualiza apenas o status via endpoint de moderação.
  Future<void> updateIssueStatus(int id, String status, {String? note}) async {
    final payload = <String, dynamic>{'status': status};
    if (note != null) payload['note'] = note;
    final response = await http.patch(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/issues/$id/status'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode(payload),
    );
    ApiClient.checkResponse(response);
  }
}
