import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

/// Configuração e utilitários HTTP compartilhados por todos os services,
/// espelhando o comportamento do interceptor Angular (auth.interceptor.ts).
class ApiClient {
  // Rodando via Chrome/web local: aponta para o backend em localhost.
  static const String baseUrl = 'http://localhost:8080';
  static const String _tokenKey = 'token';

  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  static Future<Map<String, String>> authHeaders({bool json = true}) async {
    final token = await getToken();
    final headers = <String, String>{};
    if (json) headers['Content-Type'] = 'application/json';
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    return headers;
  }

  /// Envia uma requisição multipart (usada para endpoints com upload de arquivo).
  static Future<http.StreamedResponse> multipartRequest(
    String method,
    String url,
    Map<String, String> fields, {
    List<http.MultipartFile> files = const [],
  }) async {
    final request = http.MultipartRequest(method, Uri.parse(url));
    request.headers.addAll(await authHeaders(json: false));
    request.fields.addAll(fields);
    request.files.addAll(files);
    return request.send();
  }

  static void checkResponse(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException(
        'Erro na requisição (${response.statusCode}): ${response.body}',
        statusCode: response.statusCode,
      );
    }
  }

  /// Extrai o id numérico do header `Location` (ex: `/api/project/2` -> 2),
  /// usado quando o backend responde 201/204 sem corpo.
  static int? locationId(http.Response response) {
    final location = response.headers['location'];
    if (location == null) return null;
    final segment = location
        .split('/')
        .lastWhere((s) => s.isNotEmpty, orElse: () => '');
    return int.tryParse(segment);
  }
}

class ApiException implements Exception {
  final String message;
  final int? statusCode;
  ApiException(this.message, {this.statusCode});

  @override
  String toString() => message;
}
