import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_client.dart';
import '../models/notification.dart';

class NotificationService {
  Future<List<AppNotification>> getNotifications() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/notifications'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => AppNotification.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<List<AppNotification>> getUnreadNotifications() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/notifications/unread'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => AppNotification.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<int> getUnreadCount() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/notifications/count'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return data['count'] as int? ?? 0;
  }

  Future<void> markAsRead(int id) async {
    final response = await http.patch(
      Uri.parse('${ApiClient.baseUrl}/api/notifications/$id/read'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> markAllAsRead() async {
    final response = await http.patch(
      Uri.parse('${ApiClient.baseUrl}/api/notifications/read-all'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }
}
