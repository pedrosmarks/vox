class EventImage {
  final int id;
  final String url;
  const EventImage({required this.id, required this.url});
  factory EventImage.fromJson(Map<String, dynamic> json) => EventImage(
    id: json['id'] as int? ?? 0,
    url: json['url'] as String? ?? '',
  );
}

class CivicEvent {
  final int id;
  final String title;
  final String description;
  final int categoryId;
  final double? price;
  final DateTime? startDate;
  final DateTime? endDate;
  final String location;
  final List<EventImage> images;
  const CivicEvent({
    required this.id,
    required this.title,
    required this.description,
    required this.categoryId,
    this.price,
    this.startDate,
    this.endDate,
    required this.location,
    this.images = const [],
  });
  factory CivicEvent.fromJson(Map<String, dynamic> json) => CivicEvent(
    id: json['id'] as int? ?? 0,
    title: json['title'] as String? ?? '',
    description: json['description'] as String? ?? '',
    categoryId: json['categoryId'] as int? ?? 0,
    price: (json['price'] as num?)?.toDouble(),
    startDate: DateTime.tryParse(json['startDate'] as String? ?? ''),
    endDate: DateTime.tryParse(json['endDate'] as String? ?? ''),
    location: json['location'] as String? ?? '',
    images: (json['images'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(EventImage.fromJson)
        .toList(),
  );
}

class EventPage {
  final List<CivicEvent> content;
  final int totalElements;
  const EventPage({required this.content, required this.totalElements});
  factory EventPage.fromJson(Map<String, dynamic> json) => EventPage(
    content: (json['content'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(CivicEvent.fromJson)
        .toList(),
    totalElements: json['totalElements'] as int? ?? 0,
  );
}
