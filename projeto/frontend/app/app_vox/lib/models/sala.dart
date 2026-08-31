class Sala {
  final int id;
  final String name;
  final String description;
  final int moderatorId;
  final int municipalityId;
  final String status; // 'OPEN' | 'CLOSED'
  final String createdAt;

  Sala({
    required this.id,
    required this.name,
    required this.description,
    required this.moderatorId,
    required this.municipalityId,
    required this.status,
    required this.createdAt,
  });

  factory Sala.fromJson(Map<String, dynamic> json) => Sala(
    id: json['id'] as int,
    name: json['name'] as String? ?? '',
    description: json['description'] as String? ?? '',
    moderatorId: json['moderatorId'] as int? ?? 0,
    municipalityId: json['municipalityId'] as int? ?? 0,
    status: json['status'] as String? ?? 'CLOSED',
    createdAt: json['createdAt'] as String? ?? '',
  );
}

class SolicitacaoEntrada {
  final int id;
  final int roomId;
  final int userId;
  final String status; // PENDING | APPROVED | REJECTED | REMOVED
  final bool canPublishAudio;
  final bool canPublishVideo;
  final String requestedAt;

  SolicitacaoEntrada({
    required this.id,
    required this.roomId,
    required this.userId,
    required this.status,
    required this.canPublishAudio,
    required this.canPublishVideo,
    required this.requestedAt,
  });

  factory SolicitacaoEntrada.fromJson(Map<String, dynamic> json) =>
      SolicitacaoEntrada(
        id: json['id'] as int,
        roomId: json['roomId'] as int? ?? 0,
        userId: json['userId'] as int? ?? 0,
        status: json['status'] as String? ?? 'PENDING',
        canPublishAudio: json['canPublishAudio'] as bool? ?? false,
        canPublishVideo: json['canPublishVideo'] as bool? ?? false,
        requestedAt: json['requestedAt'] as String? ?? '',
      );
}
