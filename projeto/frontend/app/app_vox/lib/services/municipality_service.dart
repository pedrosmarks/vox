import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_client.dart';

class Municipality {
  final int id;
  final String name;
  final String? state;

  Municipality({required this.id, required this.name, this.state});

  factory Municipality.fromJson(Map<String, dynamic> json) => Municipality(
    id: json['id'] as int,
    name: json['name'] as String? ?? '',
    state: json['state'] as String?,
  );

  String get label => state != null && state!.isNotEmpty ? '$name/$state' : name;
}

class MunicipalityService {
  Future<List<Municipality>> getMunicipalities() async {
    final response = await http.get(
      Uri.parse('${ApiClient.baseUrl}/api/municipality'),
      headers: await ApiClient.authHeaders(),
    );
    ApiClient.checkResponse(response);
    final list = jsonDecode(response.body) as List;
    return list
        .map((e) => Municipality.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
