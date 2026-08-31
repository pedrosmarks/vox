import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_client.dart';
import '../models/project.dart';
import '../models/user_profile.dart';

class ProjectService {
  Future<List<Project>> getProjects({String? status}) async {
    final uri = status != null
        ? Uri.parse('${ApiClient.baseUrl}/api/project?status=$status')
        : Uri.parse('${ApiClient.baseUrl}/api/project');
    final response = await http.get(
      uri,
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => Project.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<Project> getProjectById(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    return Project.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<List<Category>> getCategories() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/categories'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => Category.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<Category> getCategoryById(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/categories/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    return Category.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<List<ProjectImage>> getProjectImages(int projectId) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$projectId/image'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => ProjectImage.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<UserSummary> getUserById(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/user/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    return UserSummary.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> approveProject(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/projects/$id/approve'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> rejectProject(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/projects/$id/reject'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<List<Project>> getPendingProjects({int? page, int? size}) async {
    final params = <String>[];
    if (page != null) params.add('page=$page');
    if (size != null) params.add('size=$size');
    final query = params.isNotEmpty ? '?${params.join('&')}' : '';
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/projects/pending$query'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => Project.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<Project> createProject(
    Map<String, String> fields, {
    List<http.MultipartFile> files = const [],
  }) async {
    final streamed = await ApiClient.multipartRequest(
      'POST',
      '${ApiClient.baseUrl}/api/project',
      fields,
      files: files,
    );
    final response = await http.Response.fromStream(streamed);
    ApiClient.checkResponse(response);
    // Backend responde 201 Created só com header Location, sem corpo.
    if (response.body.trim().isEmpty) {
      final id = ApiClient.locationId(response);
      if (id != null) return getProjectById(id);
      // Sem header Location: busca o projeto mais recente do autor.
      final authorId = int.tryParse(fields['authorId'] ?? '');
      final all = await getProjects();
      final mine = authorId != null
          ? all.where((p) => p.authorId == authorId).toList()
          : all;
      if (mine.isNotEmpty) {
        mine.sort((a, b) => b.id.compareTo(a.id));
        return mine.first;
      }
      throw ApiException(
        'Projeto criado, mas não foi possível carregar os dados.',
      );
    }
    return Project.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<Project> updateProject(
    int id,
    Map<String, String> fields, {
    List<http.MultipartFile> files = const [],
  }) async {
    final streamed = await ApiClient.multipartRequest(
      'PUT',
      '${ApiClient.baseUrl}/api/project/$id',
      fields,
      files: files,
    );
    final response = await http.Response.fromStream(streamed);
    ApiClient.checkResponse(response);
    // Backend responde 204 No Content na atualização.
    if (response.body.trim().isEmpty) return getProjectById(id);
    return Project.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  // ── Vereadores vinculados ao projeto ────────────────────────

  Future<void> linkCouncilor(int projectId, int councilorId) async {
    final response = await http.post(
      Uri.parse(
        '${ApiClient.baseUrl}/api/project/$projectId/councilor/$councilorId',
      ),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> unlinkCouncilor(int projectId, int councilorId) async {
    final response = await http.delete(
      Uri.parse(
        '${ApiClient.baseUrl}/api/project/$projectId/councilor/$councilorId',
      ),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<List<UserSummary>> getProjectCouncilors(int projectId) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$projectId/councilor'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => UserSummary.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
