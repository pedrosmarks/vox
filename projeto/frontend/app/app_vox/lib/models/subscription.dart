class Subscription {
  final int id;
  final int userId;
  final String type;
  final int? targetId;

  Subscription({
    required this.id,
    required this.userId,
    required this.type,
    this.targetId,
  });

  factory Subscription.fromJson(Map<String, dynamic> json) => Subscription(
        id: json['id'] as int,
        userId: json['userId'] as int,
        type: json['type'] as String,
        targetId: json['targetId'] as int?,
      );
}
