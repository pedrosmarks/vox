import 'dart:convert';
import 'dart:typed_data';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'api_client.dart';
import '../models/user_profile.dart';

class AuthService {
  static const String _baseUrl = ApiClient.baseUrl;
  static const String _tokenKey = 'token';
  static const String _userIdKey = 'userId';
  static const String _municipalityIdKey = 'municipalityId';

  Future<String> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/authenticate'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body) as Map<String, dynamic>;
      final token = data['token'] as String;
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_tokenKey, token);
      return token;
    } else if (response.statusCode == 401) {
      throw AuthException('E-mail ou senha inválidos.');
    } else {
      throw AuthException('Erro ao conectar ao servidor. Tente novamente.');
    }
  }

  Future<UserProfile> fetchCurrentUser() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/auth/me'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final user = UserProfile.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userIdKey, user.id.toString());
    await prefs.setString(_municipalityIdKey, user.municipalityId.toString());
    return user;
  }

  Future<UserProfile> updateProfile(int id, Map<String, dynamic> data) async {
    final response = await http.put(
      Uri.parse('$_baseUrl/api/user/$id'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode(data),
    );
    ApiClient.checkResponse(response);
    return UserProfile.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<void> updatePassword(String currentPassword, String newPassword) async {
    final response = await http.put(
      Uri.parse('$_baseUrl/api/user/update-password'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode({
        'currentPassword': currentPassword,
        'newPassword': newPassword,
      }),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> forgotPassword(String email) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/auth/forgot-password'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email}),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> resetPassword(String token, String newPassword) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/auth/reset-password'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'token': token, 'newPassword': newPassword}),
    );
    ApiClient.checkResponse(response);
  }

  Future<List<UserProfile>> getCouncilors() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/users/councilors'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => UserProfile.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<UserProfile> getCouncilorById(int id) async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/users/councilors/$id'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    return UserProfile.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_userIdKey);
    await prefs.remove(_municipalityIdKey);
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }

  /// Decodifica o payload do JWT e retorna os campos (sem verificar assinatura).
  Map<String, dynamic> decodeToken(String token) {
    try {
      final parts = token.split('.');
      if (parts.length != 3) return {};
      String payload = parts[1];
      // Normaliza base64url → base64
      payload = payload.replaceAll('-', '+').replaceAll('_', '/');
      switch (payload.length % 4) {
        case 2:
          payload += '==';
          break;
        case 3:
          payload += '=';
          break;
      }
      final decoded = utf8.decode(Uint8List.fromList(base64Decode(payload)));
      return jsonDecode(decoded) as Map<String, dynamic>;
    } catch (_) {
      return {};
    }
  }

  Future<int?> getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getString(_userIdKey);
    if (stored != null) return int.tryParse(stored);
    final token = await getToken();
    if (token == null) return null;
    final payload = decodeToken(token);
    final id = payload['userId'] ?? payload['user_id'] ?? payload['sub_id'];
    return id != null ? int.tryParse(id.toString()) : null;
  }

  Future<int> getMunicipalityId() async {
    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getString(_municipalityIdKey);
    if (stored != null) return int.tryParse(stored) ?? 1;
    final token = await getToken();
    if (token == null) return 1;
    final payload = decodeToken(token);
    final id = payload['municipalityId'] ?? payload['municipality_id'];
    return id != null ? int.tryParse(id.toString()) ?? 1 : 1;
  }
}

class AuthException implements Exception {
  final String message;
  AuthException(this.message);

  @override
  String toString() => message;
}
