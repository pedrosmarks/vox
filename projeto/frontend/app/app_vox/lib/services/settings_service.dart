import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'api_client.dart';

/// Modos de acessibilidade suportados pelo backend (/api/settings).
enum AccessibilityMode {
  none,
  dark,
  highContrast,
  protanopia,
  deuteranopia,
  tritanopia;

  /// Valor enviado/recebido do backend.
  String get apiValue {
    switch (this) {
      case AccessibilityMode.none:
        return 'NONE';
      case AccessibilityMode.dark:
        return 'DARK';
      case AccessibilityMode.highContrast:
        return 'HIGH_CONTRAST';
      case AccessibilityMode.protanopia:
        return 'PROTANOPIA';
      case AccessibilityMode.deuteranopia:
        return 'DEUTERANOPIA';
      case AccessibilityMode.tritanopia:
        return 'TRITANOPIA';
    }
  }

  static AccessibilityMode fromApi(String? value) {
    switch (value) {
      case 'DARK':
        return AccessibilityMode.dark;
      case 'HIGH_CONTRAST':
        return AccessibilityMode.highContrast;
      case 'PROTANOPIA':
        return AccessibilityMode.protanopia;
      case 'DEUTERANOPIA':
        return AccessibilityMode.deuteranopia;
      case 'TRITANOPIA':
        return AccessibilityMode.tritanopia;
      default:
        return AccessibilityMode.none;
    }
  }
}

/// Configurações de aparência/acessibilidade do usuário.
@immutable
class UserSettings {
  final int fontSize; // 15–30
  final AccessibilityMode mode;

  const UserSettings({this.fontSize = 16, this.mode = AccessibilityMode.none});

  UserSettings copyWith({int? fontSize, AccessibilityMode? mode}) =>
      UserSettings(
        fontSize: fontSize ?? this.fontSize,
        mode: mode ?? this.mode,
      );

  factory UserSettings.fromJson(Map<String, dynamic> json) {
    final raw = json['fontSize'];
    final size = raw is num ? raw.round() : 16;
    return UserSettings(
      fontSize: size.clamp(15, 30),
      mode: AccessibilityMode.fromApi(json['accessibilityMode'] as String?),
    );
  }
}

/// Estado global de configurações. É um ValueNotifier para que o
/// MaterialApp reconstrua o tema quando as preferências mudarem.
class SettingsController extends ValueNotifier<UserSettings> {
  SettingsController._() : super(const UserSettings());
  static final SettingsController instance = SettingsController._();

  final _service = SettingsService();

  /// Busca as configurações do backend e atualiza o estado.
  Future<void> load() async {
    try {
      final settings = await _service.getSettings();
      value = settings;
    } catch (_) {
      // mantém o padrão em caso de falha
    }
  }

  /// Salva no backend e aplica imediatamente (otimista).
  Future<bool> save(UserSettings settings) async {
    final normalized = UserSettings(
      fontSize: settings.fontSize.clamp(15, 30),
      mode: settings.mode,
    );
    value = normalized; // aplica na hora
    try {
      await _service.updateSettings(normalized);
      return true;
    } catch (_) {
      return false;
    }
  }

  /// Restaura padrão (usado no logout).
  void reset() {
    value = const UserSettings();
  }
}

class SettingsService {
  Future<UserSettings> getSettings() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/settings'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    if (response.body.trim().isEmpty) return const UserSettings();
    return UserSettings.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> updateSettings(UserSettings settings) async {
    final response = await http.put(
      Uri.parse('${ApiClient.baseUrl}/api/settings'),
      headers: await ApiClient.authHeaders(),
      body: jsonEncode({
        'fontSize': settings.fontSize,
        'accessibilityMode': settings.mode.apiValue,
      }),
    );
    ApiClient.checkResponse(response);
  }
}
