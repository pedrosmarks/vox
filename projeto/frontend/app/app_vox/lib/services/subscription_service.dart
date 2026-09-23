import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_client.dart';
import '../models/subscription.dart';

class SubscriptionService {
  Future<List<Subscription>> getSubscriptions() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/subscriptions'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => Subscription.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> subscribeAllProjects() => _post('/api/subscriptions/all-projects');
  Future<void> unsubscribeAllProjects() => _delete('/api/subscriptions/all-projects');

  Future<void> subscribeAllIssues() => _post('/api/subscriptions/all-issues');
  Future<void> unsubscribeAllIssues() => _delete('/api/subscriptions/all-issues');

  Future<void> subscribeProject(int projectId) =>
      _post('/api/subscriptions/projects/$projectId');
  Future<void> unsubscribeProject(int projectId) =>
      _delete('/api/subscriptions/projects/$projectId');

  Future<void> subscribeIssue(int issueId) =>
      _post('/api/subscriptions/issues/$issueId');
  Future<void> unsubscribeIssue(int issueId) =>
      _delete('/api/subscriptions/issues/$issueId');

  Future<void> subscribeCategory(int categoryId) =>
      _post('/api/subscriptions/categories/$categoryId');
  Future<void> unsubscribeCategory(int categoryId) =>
      _delete('/api/subscriptions/categories/$categoryId');

  Future<void> subscribeCouncilor(int councilorId) =>
      _post('/api/subscriptions/councilors/$councilorId');
  Future<void> unsubscribeCouncilor(int councilorId) =>
      _delete('/api/subscriptions/councilors/$councilorId');

  Future<void> _post(String path) async {
    final response = await http.post(
      Uri.parse('${ApiClient.baseUrl}$path'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }

  Future<void> _delete(String path) async {
    final response = await http.delete(
      Uri.parse('${ApiClient.baseUrl}$path'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
  }
}
