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

  Future<void> approveProject(int id, {String? feedback}) async {
    final hasFeedback = feedback != null && feedback.trim().isNotEmpty;
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/projects/$id/approve'),
      headers: await ApiClient.authHeaders(),
      body: hasFeedback ? jsonEncode({'feedback': feedback.trim()}) : null,
    );
    ApiClient.checkResponse(response);
  }

  /// Rejeita o projeto com um comentário visível ao cidadão.
  /// Usa PATCH /status porque é o endpoint que persiste o `note` no histórico
  /// (o /reject não registra o comentário de forma recuperável).
  Future<void> rejectProject(int id, {String? feedback}) async {
    await updateProjectStatus(id, 'REJECTED', note: feedback);
  }

  /// Histórico de mudanças de status do projeto (contém o comentário/note).
  Future<List<ProjectHistoryEntry>> getProjectHistory(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/history'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => ProjectHistoryEntry.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// Comentário de rejeição/cancelamento mais recente do histórico.
  Future<String> getRejectionNote(int id) async {
    try {
      final history = await getProjectHistory(id);
      final rejections =
          history
              .where(
                (h) => h.newStatus == 'REJECTED' || h.newStatus == 'CANCELLED',
              )
              .toList()
            ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return rejections.isNotEmpty ? rejections.first.note.trim() : '';
    } catch (_) {
      return '';
    }
  }

  /// Atualiza o status do projeto (moderador/admin). [note] é um comentário
  /// opcional associado à mudança de status.
  Future<void> updateProjectStatus(
    int id,
    String status, {
    String? note,
  }) async {
    final body = <String, dynamic>{'status': status};
    if (note != null && note.trim().isNotEmpty) body['note'] = note.trim();
    final response = await http.patch(
      Uri.parse('${ApiClient.baseUrl}/api/moderation/projects/$id/status'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode(body),
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

  // ── Assinaturas de apoio ao projeto (petição) ───────────────

  /// Assina (apoia) o projeto.
  Future<void> signProject(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/signature'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  /// Remove a assinatura de apoio do projeto.
  Future<void> unsignProject(int id) async {
    final response = await http.delete(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/signature'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  /// Verifica se o usuário atual já assinou o projeto.
  Future<bool> hasSignedProject(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/signature/me'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return data['signed'] as bool? ?? false;
  }

  /// Contagem total de assinaturas do projeto.
  Future<int> getSignatureCount(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/signature/count'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return data['total'] as int? ?? 0;
  }

  // ── Opiniões / Apoios (opinion APPROVE) ─────────────────────

  /// Registra a opinião do usuário. APPROVE = apoiar; NEUTRAL = remover apoio.
  Future<void> setOpinion(int id, String opinion) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/opinion'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode({'opinion': opinion}),
    );
    ApiClient.checkResponse(response);
  }

  /// Nº de apoios (opinion APPROVE) do projeto. O backend retorna
  /// approved/disapproved/neutral/total.
  Future<int> getApprovalCount(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/opinion/stats'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return data['approved'] as int? ?? 0;
  }

  /// Retorna a opinião do usuário atual ('APPROVE'/'DISAPPROVE'/'NEUTRAL') ou
  /// null se ainda não opinou (backend responde 404).
  Future<String?> getMyOpinion(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/project/$id/opinion/me'),
      headers: await ApiClient.authHeaders(),
    );
    if (response.statusCode == 404) return null;
    if (response.statusCode < 200 || response.statusCode >= 300) return null;
    if (response.body.trim().isEmpty) return null;
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return data['opinion'] as String?;
  }
}
