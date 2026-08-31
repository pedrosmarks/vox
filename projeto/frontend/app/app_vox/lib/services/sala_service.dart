import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_client.dart';
import '../models/sala.dart';

/// Espelha sala.service.ts do site — salas de audiência pública (LiveKit).
class SalaService {
  Future<Sala> createSala(String name, String description) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/salas'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode({'name': name, 'description': description}),
    );
    ApiClient.checkResponse(response);
    if (response.body.trim().isEmpty) {
      final id = ApiClient.locationId(response);
      if (id != null) return getSalaById(id);
    }
    return Sala.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<List<Sala>> getSalas() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/salas'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list.map((e) => Sala.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<Sala> getSalaById(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/salas/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    return Sala.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<void> encerrarSala(int id) async {
    final response = await http.delete(
      Uri.parse('${ApiClient.baseUrl}/api/salas/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> solicitarEntrada(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/salas/$id/solicitacoes-entrada'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<List<SolicitacaoEntrada>> getSolicitacoesEntrada(int id) async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/salas/$id/solicitacoes-entrada'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => SolicitacaoEntrada.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> aprovarSolicitacao(int id, int participanteId) =>
      _post('/api/salas/$id/solicitacoes-entrada/$participanteId/aprovar');

  Future<void> rejeitarSolicitacao(int id, int participanteId) =>
      _post('/api/salas/$id/solicitacoes-entrada/$participanteId/rejeitar');

  Future<void> liberarMicrofone(int id, int participanteId) =>
      _post('/api/salas/$id/participantes/$participanteId/microfone/liberar');

  Future<void> bloquearMicrofone(int id, int participanteId) =>
      _post('/api/salas/$id/participantes/$participanteId/microfone/bloquear');

  Future<void> liberarCamera(int id, int participanteId) =>
      _post('/api/salas/$id/participantes/$participanteId/camera/liberar');

  Future<void> bloquearCamera(int id, int participanteId) =>
      _post('/api/salas/$id/participantes/$participanteId/camera/bloquear');

  Future<void> expulsarParticipante(int id, int participanteId) async {
    final response = await http.delete(
      Uri.parse(
        '${ApiClient.baseUrl}/api/salas/$id/participantes/$participanteId',
      ),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<String> gerarToken(int id) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}/api/salas/$id/token'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return data['token'] as String;
  }

  Future<void> _post(String path) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}$path'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }
}
